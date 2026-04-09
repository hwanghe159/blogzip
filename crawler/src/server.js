const express = require("express");
const { chromium } = require("playwright");

const app = express();
app.use(express.json({ limit: "2mb" }));

function buildRequestPayloadLog(req) {
  if (!req.body || typeof req.body !== "object") {
    return "";
  }

  let payload;
  if (req.path === "/articles/fetch") {
    const articleUrls = Array.isArray(req.body.articleUrls) ? req.body.articleUrls : [];
    payload = {
      blogUrl: req.body.blogUrl || null,
      cssSelector: req.body.cssSelector || null,
      articleUrlsCount: articleUrls.length,
      articleUrlsPreview: articleUrls.slice(0, 3),
    };
  } else if (req.path === "/metadata/fetch" || req.path === "/content/fetch") {
    payload = {
      url: req.body.url || null,
    };
  } else {
    payload = req.body;
  }

  const serialized = JSON.stringify(payload);
  if (serialized.length > 500) {
    return ` payload=${serialized.slice(0, 500)}...(truncated)`;
  }
  return ` payload=${serialized}`;
}

app.use((req, res, next) => {
  const start = Date.now();
  const requestId = Math.random().toString(36).slice(2, 10);
  const payloadLog = buildRequestPayloadLog(req);
  console.log(`[crawler][${requestId}] -> ${req.method} ${req.originalUrl}${payloadLog}`);

  res.on("finish", () => {
    const elapsed = Date.now() - start;
    console.log(
      `[crawler][${requestId}] <- ${req.method} ${req.originalUrl} ${res.statusCode} (${elapsed}ms)`
    );
  });

  next();
});

const PORT = Number(process.env.PORT || 8090);
const HOST = process.env.HOST || "0.0.0.0";
const HEADLESS = process.env.HEADLESS !== "false";
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 30000);
const CRAWLER_UA =
  process.env.CRAWLER_USER_AGENT ||
  "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

const RSS_POSTFIXES = ["/rss", "/feed", "/rss.xml", "/feed.xml"];
const RSS_CONTENT_TYPES = new Set([
  "application/xml",
  "application/rss+xml",
  "application/atom+xml",
  "text/xml",
]);

const chromeArgs = (process.env.CHROME_ARGS || "")
  .split(" ")
  .map((arg) => arg.trim())
  .filter(Boolean);

const browserPromise = chromium.launch({
  headless: HEADLESS,
  executablePath: process.env.CHROME_BIN || undefined,
  args: chromeArgs,
});

let serialized = Promise.resolve();

function runSerialized(task) {
  const run = serialized.then(task, task);
  serialized = run.catch(() => undefined);
  return run;
}

function normalizeUrl(input) {
  const url = new URL(input);
  return url.toString();
}

async function withPage(work) {
  const browser = await browserPromise;
  const context = await browser.newContext({
    userAgent: CRAWLER_UA,
    viewport: { width: 1920, height: 1080 },
  });
  const page = await context.newPage();
  try {
    page.setDefaultTimeout(REQUEST_TIMEOUT_MS);
    return await work(page, context);
  } finally {
    await context.close();
  }
}

async function gotoAndWait(page, url) {
  await page.goto(url, { waitUntil: "domcontentloaded", timeout: REQUEST_TIMEOUT_MS });
  await page.waitForLoadState("networkidle", { timeout: REQUEST_TIMEOUT_MS }).catch(() => undefined);
}

function guessRssUrl(urlString) {
  try {
    const url = new URL(urlString);
    if (url.hostname === "velog.io") {
      const match = url.pathname.match(/@([^/]+)/);
      if (match && match[1]) {
        return `https://v2.velog.io/rss/${match[1]}`;
      }
    }

    if (url.hostname === "medium.com") {
      return `https://medium.com/feed${url.pathname}`;
    }
  } catch (_error) {
    return null;
  }

  return null;
}

async function probeRssUrl(urlString) {
  const base = urlString.endsWith("/") ? urlString.slice(0, -1) : urlString;
  for (const postfix of RSS_POSTFIXES) {
    const candidate = `${base}${postfix}`;
    try {
      const response = await fetch(candidate, {
        method: "GET",
        redirect: "follow",
        signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
      });
      const contentType = (response.headers.get("content-type") || "").split(";")[0].trim();
      if (RSS_CONTENT_TYPES.has(contentType)) {
        return candidate;
      }
    } catch (_error) {
      // Ignore and continue probing.
    }
  }
  return null;
}

async function getMetadata(urlString) {
  return withPage(async (page) => {
    let title = urlString;
    let imageUrl = null;
    let rss = null;

    await gotoAndWait(page, urlString);
    const currentUrl = page.url();

    const pageTitle = await page.title().catch(() => "");
    if (pageTitle && pageTitle.trim().length > 0) {
      title = pageTitle;
    }

    imageUrl = await page
      .locator("meta[property='og:image']")
      .first()
      .getAttribute("content")
      .catch(() => null);

    if (imageUrl && !imageUrl.startsWith("http")) {
      imageUrl = new URL(imageUrl, currentUrl).toString();
    }

    const rssCandidates = await page.$$eval(
      "link[type='application/rss+xml'], link[type='application/atom+xml']",
      (elements) =>
        elements
          .map((element) => element.href || element.getAttribute("href"))
          .filter((href) => !!href)
    );

    if (rssCandidates.length > 0) {
      rss = rssCandidates[0];
    } else {
      rss = guessRssUrl(urlString);
      if (!rss) {
        rss = await probeRssUrl(urlString);
      }
    }

    return { title, imageUrl, rss };
  });
}

async function getContent(urlString) {
  return withPage(async (page) => {
    await gotoAndWait(page, urlString);
    const content = await page.content();
    return { content };
  });
}

async function extractArticleUrl(element, baseUrl) {
  let href = await element.getAttribute("href");
  if (!href) {
    const anchor = await element.$("a[href]");
    if (anchor) {
      href = await anchor.getAttribute("href");
    }
  }
  if (!href) {
    return null;
  }
  return new URL(href, baseUrl).toString();
}

async function getArticles(payload) {
  const blogUrl = payload.blogUrl;
  const cssSelector = payload.cssSelector;
  const knownArticleUrls = new Set(payload.articleUrls || []);

  return withPage(async (page, context) => {
    const articles = [];
    let failCause = null;

    try {
      await gotoAndWait(page, blogUrl);
      await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
      await page.waitForSelector(cssSelector, { timeout: REQUEST_TIMEOUT_MS });
      const elements = await page.$$(cssSelector);

      for (const element of elements) {
        const title = (await element.innerText().catch(() => "")).trim();
        if (!title) {
          continue;
        }

        const articleUrl = await extractArticleUrl(element, page.url());
        if (!articleUrl) {
          continue;
        }

        if (knownArticleUrls.has(articleUrl)) {
          break;
        }

        const articlePage = await context.newPage();
        try {
          await gotoAndWait(articlePage, articleUrl);
          const content = await articlePage.content();
          articles.push({
            title,
            url: articleUrl,
            content,
          });
        } finally {
          await articlePage.close();
        }
      }
    } catch (error) {
      failCause = {
        message: error instanceof Error ? error.message : String(error),
      };
    }

    return {
      articles,
      failCause,
    };
  });
}

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.post("/metadata/fetch", async (req, res) => {
  const inputUrl = req.body?.url;
  if (!inputUrl) {
    res.status(400).json({ message: "url is required" });
    return;
  }

  try {
    const normalizedUrl = normalizeUrl(inputUrl);
    const result = await runSerialized(() => getMetadata(normalizedUrl));
    res.json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    res.status(500).json({ message });
  }
});

app.post("/content/fetch", async (req, res) => {
  const inputUrl = req.body?.url;
  if (!inputUrl) {
    res.status(400).json({ message: "url is required" });
    return;
  }

  try {
    const normalizedUrl = normalizeUrl(inputUrl);
    const result = await runSerialized(() => getContent(normalizedUrl));
    res.json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    res.status(500).json({ message });
  }
});

app.post("/articles/fetch", async (req, res) => {
  const blogUrl = req.body?.blogUrl;
  const cssSelector = req.body?.cssSelector;
  if (!blogUrl || !cssSelector) {
    res.status(400).json({ message: "blogUrl and cssSelector are required" });
    return;
  }

  try {
    const normalizedBlogUrl = normalizeUrl(blogUrl);
    const result = await runSerialized(() =>
      getArticles({
        blogUrl: normalizedBlogUrl,
        cssSelector,
        articleUrls: Array.isArray(req.body?.articleUrls) ? req.body.articleUrls : [],
      })
    );
    res.json(result);
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    res.status(500).json({ articles: [], failCause: { message } });
  }
});

let server = app.listen(PORT, HOST, () => {
  console.log(`[crawler] listening on ${HOST}:${PORT}`);
});

server.on("error", (error) => {
  if (error && error.code === "EPERM" && HOST !== "127.0.0.1") {
    console.warn(
      `[crawler] failed to bind ${HOST}:${PORT} with EPERM. retrying on 127.0.0.1:${PORT}`
    );
    server = app.listen(PORT, "127.0.0.1", () => {
      console.log(`[crawler] listening on 127.0.0.1:${PORT}`);
    });
    return;
  }
  throw error;
});

async function shutdown(signal) {
  console.log(`[crawler] received ${signal}, shutting down`);
  server.close(async () => {
    try {
      const browser = await browserPromise;
      await browser.close();
    } catch (_error) {
      // no-op
    }
    process.exit(0);
  });
}

process.on("SIGINT", () => {
  shutdown("SIGINT");
});
process.on("SIGTERM", () => {
  shutdown("SIGTERM");
});
