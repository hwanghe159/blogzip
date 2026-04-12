import {
  AdminArticleReportResponse,
  AdminArticleReportStatus,
  AdminArticleCreatedDateUpdateResponse,
  AdminArticleResummaryApplyResponse,
  AdminArticleResummaryPreviewResponse,
  AdminArticleResponse,
  AdminBlogRequiringSelectorResponse,
  AdminArticleSummaryResponse,
  AdminBlogCssSelectorUpdateResponse,
  AdminCssSelectorSuggestResponse,
  AdminCssSelectorTestResponse,
  AdminKeywordHeadUpdateResponse,
  AdminKeywordCreateResponse,
  AdminKeywordOverviewResponse,
  AdminRecentArticleResponse,
  ArticleReportResponse,
  ApiErrorResponse,
  ArticleResponse,
  BlogResponse,
  BlogCreateResponse,
  DayOfWeek,
  FeedKeywordCountResponse,
  LoginResponse,
  PaginationResponse,
  ReadLaterDetailResponse,
  SubscriptionResponse,
  UserResponse,
} from "./types";

const ACCESS_TOKEN_KEY = "blogzip_access_token";
const PROFILE_IMAGE_KEY = "blogzip_profile_image";
const FALLBACK_GOOGLE_CLIENT_ID =
  "538244264177-f2a956r71n7g6cp1tomqfmcsvdu6nhcp.apps.googleusercontent.com";

const API_BASE_URL = (process.env.REACT_APP_API_BASE_URL ?? "").replace(/\/+$/, "");

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public code: string | null = null
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  token?: string | null;
  query?: Record<string, string | number | Array<string | number> | null | undefined>;
};

function buildQuery(query?: RequestOptions["query"]): string {
  if (!query) return "";
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value === null || value === undefined || value === "") return;
    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item === "") return;
        params.append(key, String(item));
      });
      return;
    }
    params.set(key, String(value));
  });
  const queryString = params.toString();
  return queryString ? `?${queryString}` : "";
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, token, query } = options;
  const url = `${API_BASE_URL}${path}${buildQuery(query)}`;

  const response = await fetch(url, {
    method,
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    let payload: ApiErrorResponse | null = null;
    try {
      payload = (await response.json()) as ApiErrorResponse;
    } catch (_) {
      payload = null;
    }
    throw new ApiError(
      payload?.message ?? `요청 실패 (${response.status})`,
      response.status,
      payload?.code ?? null
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export function saveAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function clearAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(PROFILE_IMAGE_KEY);
}

export function saveProfileImage(profileImage: string): void {
  localStorage.setItem(PROFILE_IMAGE_KEY, profileImage);
}

export function getProfileImage(): string | null {
  return localStorage.getItem(PROFILE_IMAGE_KEY);
}

export function buildGoogleAuthUrl(): string {
  const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID ?? FALLBACK_GOOGLE_CLIENT_ID;
  const redirectUri =
    process.env.REACT_APP_GOOGLE_LOGIN_REDIRECT_URI ??
    `${window.location.origin}/login/google`;
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: redirectUri,
    response_type: "code",
    scope: "openid profile email",
    access_type: "offline",
    prompt: "select_account",
  });
  return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
}

export async function loginWithGoogleCode(code: string): Promise<LoginResponse> {
  return request<LoginResponse>("/api/v1/login/google", {
    query: { code },
  });
}

export async function getMyProfile(token: string): Promise<UserResponse> {
  return request<UserResponse>("/api/v1/me", { token });
}

export async function updateReceiveDays(
  token: string,
  receiveDays: DayOfWeek[]
): Promise<UserResponse> {
  return request<UserResponse>("/api/v1/me", {
    method: "PUT",
    token,
    body: { receiveDays },
  });
}

export async function withdrawMe(token: string): Promise<void> {
  await request<void>("/api/v1/me", {
    method: "DELETE",
    token,
  });
}

export async function getArticles(params: {
  token?: string | null;
  from: string;
  to?: string;
  next?: number | null;
  size?: number;
  myOnly?: boolean;
  keywords?: string[];
}): Promise<PaginationResponse<ArticleResponse>> {
  const { token, from, to, next, size = 20, myOnly = false, keywords } = params;
  return request<PaginationResponse<ArticleResponse>>(
    myOnly ? "/api/v1/my/article" : "/api/v1/article",
    {
      token: token ?? null,
      query: {
        from,
        to,
        next,
        size,
        keywords,
      },
    }
  );
}

export async function getFeedKeywordCounts(params: {
  token?: string | null;
  from: string;
  to?: string;
  myOnly?: boolean;
}): Promise<FeedKeywordCountResponse[]> {
  const { token, from, to, myOnly = false } = params;
  return request<FeedKeywordCountResponse[]>(
    myOnly ? "/api/v1/my/article/keyword-count" : "/api/v1/article/keyword-count",
    {
      token: token ?? null,
      query: {
        from,
        to,
      },
    }
  );
}

export async function getReadLater(params: {
  token: string;
  next?: number | null;
  size?: number;
}): Promise<PaginationResponse<ReadLaterDetailResponse>> {
  return request<PaginationResponse<ReadLaterDetailResponse>>("/api/v1/read-later", {
    token: params.token,
    query: { next: params.next, size: params.size ?? 20 },
  });
}

export async function addReadLater(token: string, articleId: number): Promise<void> {
  await request<void>("/api/v1/read-later", {
    method: "POST",
    token,
    body: { articleId },
  });
}

export async function removeReadLater(token: string, articleId: number): Promise<void> {
  await request<void>("/api/v1/read-later", {
    method: "DELETE",
    token,
    body: { articleId },
  });
}

export async function markArticleRead(token: string, articleId: number): Promise<void> {
  await request<void>(`/api/v1/article/${articleId}/read`, {
    method: "POST",
    token,
  });
}

export async function reportArticle(
  token: string,
  articleId: number,
  reason: string,
  detail?: string
): Promise<ArticleReportResponse> {
  return request<ArticleReportResponse>(`/api/v1/article/${articleId}/report`, {
    method: "POST",
    token,
    body: {
      reason,
      detail,
    },
  });
}

export async function searchBlogs(query: string): Promise<SubscriptionResponse["blog"][]> {
  return request<SubscriptionResponse["blog"][]>("/api/v1/blog/search", {
    query: { query },
  });
}

export async function getAllBlogs(): Promise<BlogResponse[]> {
  return request<BlogResponse[]>("/api/v1/blog");
}

export async function createBlog(
  token: string,
  url: string
): Promise<BlogCreateResponse> {
  return request<BlogCreateResponse>("/api/v1/blog", {
    method: "POST",
    token,
    body: { url },
  });
}

export async function getSubscriptions(token: string): Promise<SubscriptionResponse[]> {
  return request<SubscriptionResponse[]>("/api/v1/subscription", { token });
}

export async function subscribeBlog(
  token: string,
  blogId: number
): Promise<SubscriptionResponse> {
  return request<SubscriptionResponse>("/api/v1/subscription", {
    method: "POST",
    token,
    body: { blogId },
  });
}

export async function unsubscribeBlog(token: string, blogId: number): Promise<void> {
  await request<void>("/api/v1/subscription", {
    method: "DELETE",
    token,
    body: { blogId },
  });
}

export async function getAdminKeywordOverview(
  token: string
): Promise<AdminKeywordOverviewResponse> {
  return request<AdminKeywordOverviewResponse>("/api/admin/keyword/overview", { token });
}

export async function updateAdminKeyword(
  token: string,
  currentValue: string,
  payload: {
    value?: string | null;
    isVisible?: boolean | null;
  }
): Promise<AdminKeywordOverviewResponse> {
  return request<AdminKeywordOverviewResponse>(`/api/admin/keyword/${encodeURIComponent(currentValue)}`, {
    method: "PATCH",
    token,
    body: payload,
  });
}

export async function updateAdminKeywordById(
  token: string,
  keywordId: number,
  payload: {
    value?: string | null;
    isVisible?: boolean | null;
  }
): Promise<AdminKeywordOverviewResponse> {
  return request<AdminKeywordOverviewResponse>(`/api/admin/keyword/id/${keywordId}`, {
    method: "PATCH",
    token,
    body: payload,
  });
}

export async function mergeAdminKeywords(
  token: string,
  src: string,
  dest: string
): Promise<AdminKeywordOverviewResponse> {
  return request<AdminKeywordOverviewResponse>("/api/admin/keyword/merge", {
    method: "POST",
    token,
    query: { src, dest },
  });
}

export async function mergeAdminKeywordsById(
  token: string,
  srcKeywordId: number,
  destKeywordId: number
): Promise<AdminKeywordOverviewResponse> {
  return request<AdminKeywordOverviewResponse>("/api/admin/keyword/merge/by-id", {
    method: "POST",
    token,
    body: { srcKeywordId, destKeywordId },
  });
}

export async function updateAdminKeywordHead(
  token: string,
  keywordId: number,
  headKeywordId: number | null
): Promise<AdminKeywordHeadUpdateResponse> {
  return request<AdminKeywordHeadUpdateResponse>(`/api/admin/keyword/${keywordId}/head`, {
    method: "PATCH",
    token,
    body: { headKeywordId },
  });
}

export async function createAdminKeyword(
  token: string,
  payload: {
    value: string;
    isVisible?: boolean;
  }
): Promise<AdminKeywordCreateResponse> {
  return request<AdminKeywordCreateResponse>("/api/admin/keyword", {
    method: "POST",
    token,
    body: payload,
  });
}

export async function getAdminRecentArticles(
  token: string,
  params: { next?: number | null; size?: number } = {}
): Promise<PaginationResponse<AdminRecentArticleResponse>> {
  return request<PaginationResponse<AdminRecentArticleResponse>>("/api/admin/article/recent", {
    token,
    query: {
      next: params.next,
      size: params.size ?? 20,
    },
  });
}

export async function getAdminArticleReports(
  token: string,
  articleId: number
): Promise<AdminArticleReportResponse[]> {
  return request<AdminArticleReportResponse[]>(`/api/admin/article/${articleId}/report`, {
    token,
  });
}

export async function getAdminReceivedReports(
  token: string,
  params: { next?: number | null; size?: number } = {}
): Promise<PaginationResponse<AdminArticleReportResponse>> {
  return request<PaginationResponse<AdminArticleReportResponse>>("/api/admin/report/received", {
    token,
    query: {
      next: params.next,
      size: params.size ?? 20,
    },
  });
}

export async function updateAdminArticleReportStatus(
  token: string,
  reportId: number,
  status: AdminArticleReportStatus
): Promise<AdminArticleReportResponse> {
  return request<AdminArticleReportResponse>(`/api/admin/report/${reportId}/status`, {
    method: "PATCH",
    token,
    body: { status },
  });
}

export async function updateAdminArticleCreatedDate(
  token: string,
  articleId: number,
  createdDate: string
): Promise<AdminArticleCreatedDateUpdateResponse> {
  return request<AdminArticleCreatedDateUpdateResponse>(
    `/api/admin/article/${articleId}/created-date`,
    {
      method: "PATCH",
      token,
      body: { createdDate },
    }
  );
}

export async function updateAdminArticleCreatedDates(
  token: string,
  items: { articleId: number; createdDate: string }[]
): Promise<AdminArticleCreatedDateUpdateResponse[]> {
  return request<AdminArticleCreatedDateUpdateResponse[]>("/api/admin/article/created-date", {
    method: "PATCH",
    token,
    body: { items },
  });
}

export async function getAdminArticleSummaries(
  token: string,
  articleId: number
): Promise<AdminArticleSummaryResponse[]> {
  return request<AdminArticleSummaryResponse[]>(`/api/admin/article/${articleId}/summary`, {
    token,
  });
}

export async function applyAdminArticleSummary(
  token: string,
  summaryId: number
): Promise<AdminArticleSummaryResponse> {
  return request<AdminArticleSummaryResponse>(`/api/admin/article/summary/${summaryId}/apply`, {
    method: "PATCH",
    token,
  });
}

export async function addAdminArticleKeywords(
  token: string,
  articleId: number,
  values: string[]
): Promise<AdminArticleResponse> {
  return request<AdminArticleResponse>(`/api/admin/article/${articleId}/keyword`, {
    method: "POST",
    token,
    body: { values },
  });
}

export async function previewAdminArticleResummary(
  token: string,
  articleIds: number[]
): Promise<AdminArticleResummaryPreviewResponse> {
  return request<AdminArticleResummaryPreviewResponse>("/api/admin/article/re-summary/preview", {
    method: "POST",
    token,
    body: { articleIds },
  });
}

export async function applyAdminArticleResummary(
  token: string,
  items: {
    articleSummaryId: number;
    keywords: string[];
    applyKeywords: boolean;
  }[]
): Promise<AdminArticleResummaryApplyResponse> {
  return request<AdminArticleResummaryApplyResponse>("/api/admin/article/re-summary/apply", {
    method: "POST",
    token,
    body: { items },
  });
}

export async function testAdminCssSelector(
  token: string,
  payload: {
    blogUrl: string;
    cssSelector: string;
    sampleSize?: number;
  }
): Promise<AdminCssSelectorTestResponse> {
  return request<AdminCssSelectorTestResponse>("/api/admin/crawler/css-selector/test", {
    method: "POST",
    token,
    body: payload,
  });
}

export async function suggestAdminCssSelector(
  token: string,
  payload: {
    blogUrl: string;
    candidateLimit?: number;
    sampleSize?: number;
    firstArticleTitle?: string;
    firstArticleUrl?: string;
  }
): Promise<AdminCssSelectorSuggestResponse> {
  return request<AdminCssSelectorSuggestResponse>("/api/admin/crawler/css-selector/suggest", {
    method: "POST",
    token,
    body: payload,
  });
}

export async function getAdminBlogsRequiringSelector(
  token: string
): Promise<AdminBlogRequiringSelectorResponse[]> {
  return request<AdminBlogRequiringSelectorResponse[]>("/api/admin/blog/requiring-selector", {
    token,
  });
}

export async function updateAdminBlogCssSelector(
  token: string,
  blogId: number,
  payload: {
    cssSelector: string;
    sampleSize?: number;
    force?: boolean;
  }
): Promise<AdminBlogCssSelectorUpdateResponse> {
  return request<AdminBlogCssSelectorUpdateResponse>(`/api/admin/blog/${blogId}/css-selector`, {
    method: "PATCH",
    token,
    body: payload,
  });
}

export async function updateAdminBlogCssSelectorByUrl(
  token: string,
  payload: {
    blogUrl: string;
    cssSelector: string;
    sampleSize?: number;
    force?: boolean;
  }
): Promise<AdminBlogCssSelectorUpdateResponse> {
  return request<AdminBlogCssSelectorUpdateResponse>("/api/admin/blog/css-selector/by-url", {
    method: "POST",
    token,
    body: payload,
  });
}
