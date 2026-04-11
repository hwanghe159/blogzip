export type DayOfWeek =
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export interface BlogResponse {
  id: number;
  name: string;
  url: string;
  image: string | null;
  rssStatus: "NO_RSS" | "WITH_CONTENT" | "WITHOUT_CONTENT";
  rss: string | null;
  isShowOnMain: boolean;
}

export interface BlogCreateResponse {
  id: number;
  name: string;
  url: string;
  image: string | null;
  rss: string | null;
}

export interface ArticleResponse {
  id: number;
  blog: BlogResponse;
  title: string;
  url: string;
  summary: string;
  isReadLater: boolean;
  keywords: string[];
  createdDate: string;
}

export interface ReadLaterArticleResponse {
  id: number;
  blog: BlogResponse;
  title: string;
  url: string;
  summary: string;
  keywords: string[];
  createdDate: string;
}

export interface ReadLaterDetailResponse {
  id: number;
  article: ReadLaterArticleResponse;
}

export interface PaginationResponse<T> {
  items: T[];
  next: number | null;
}

export interface UserResponse {
  id: number;
  email: string;
  receiveDays: DayOfWeek[];
  createdAt: string;
  updatedAt: string;
}

export interface LoginResponse {
  id: number;
  accessToken: string;
  email: string;
  image: string;
}

export interface SubscriptionResponse {
  id: number;
  blog: BlogResponse;
  createdAt: string;
}

export interface ApiErrorResponse {
  code: string | null;
  message: string | null;
}

export interface SessionState {
  status: "booting" | "guest" | "authenticated";
  token: string | null;
  profileImage: string | null;
  user: UserResponse | null;
}
