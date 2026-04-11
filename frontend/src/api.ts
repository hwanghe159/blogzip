import {
  ApiErrorResponse,
  ArticleResponse,
  BlogResponse,
  BlogCreateResponse,
  DayOfWeek,
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
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  token?: string | null;
  query?: Record<string, string | number | null | undefined>;
};

function buildQuery(query?: RequestOptions["query"]): string {
  if (!query) return "";
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value === null || value === undefined || value === "") return;
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

export async function getArticles(params: {
  token?: string | null;
  from: string;
  to?: string;
  next?: number | null;
  size?: number;
  myOnly?: boolean;
}): Promise<PaginationResponse<ArticleResponse>> {
  const { token, from, to, next, size = 20, myOnly = false } = params;
  return request<PaginationResponse<ArticleResponse>>(
    myOnly ? "/api/v1/my/article" : "/api/v1/article",
    {
      token: token ?? null,
      query: {
        from,
        to,
        next,
        size,
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
