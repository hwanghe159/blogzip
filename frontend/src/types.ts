export interface BlogResponse {
  id: number;
  name: string;
  url: string;
  image: string | null;
  rssStatus: string;
  rss: string | null;
  isShowOnMain?: boolean;
  createdBy?: number;
  createdAt?: string;
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

export interface PaginationResponse<T> {
  items: T[];
  next: number | null;
}

export interface SubscriptionResponse {
  id: number;
  blog: BlogResponse;
  createdAt: string;
}

export interface ReadLaterResponse {
  id: number;
  article: ArticleResponse;
}

export interface LoginResponse {
  id: number;
  email: string;
  image: string;
  accessToken: string;
}

export interface UserResponse {
  id: number;
  email: string;
  receiveDays: string[];
  createdAt: string;
  updatedAt: string;
}
