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
  isAdmin: boolean;
  receiveDays: DayOfWeek[];
  createdAt: string;
  updatedAt: string;
}

export interface ArticleReportResponse {
  id: number;
  articleId: number;
  userId: number;
  reason: string;
  detail: string | null;
  status: string;
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

export interface FeedKeywordCountResponse {
  keyword: string;
  count: number;
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

export interface AdminFollowerKeywordOverviewResponse {
  id: number;
  value: string;
  isVisible: boolean;
  createdAt: string;
}

export interface AdminHeadKeywordOverviewResponse {
  id: number;
  value: string;
  isVisible: boolean;
  articleCount: number;
  createdAt: string;
  followers: AdminFollowerKeywordOverviewResponse[];
}

export interface AdminKeywordOverviewResponse {
  totalKeywordCount: number;
  headKeywordCount: number;
  followerKeywordCount: number;
  visibleKeywordCount: number;
  hiddenKeywordCount: number;
  headKeywords: AdminHeadKeywordOverviewResponse[];
}

export interface AdminArticleCreatedDateUpdateResponse {
  articleId: number;
  beforeCreatedDate: string | null;
  createdDate: string;
}

export interface AdminArticleSummaryResponse {
  id: number;
  articleId: number;
  summary: string;
  summarizedBy: string;
  isApplied: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminFollowerKeywordResponse {
  id: number;
  value: string;
  createdAt: string;
}

export interface AdminHeadKeywordResponse {
  id: number;
  value: string;
  followers: AdminFollowerKeywordResponse[];
  createdAt: string;
}

export interface AdminArticleResponse {
  id: number;
  blogId: number;
  title: string;
  content: string;
  url: string;
  summary: string | null;
  summarizedBy: string | null;
  createdDate: string | null;
  keywords: AdminHeadKeywordResponse[];
}

export interface AdminArticleResummaryPreviewItemResponse {
  articleId: number;
  title: string | null;
  candidateSummaryId: number | null;
  currentSummary: string | null;
  currentSummarizedBy: string | null;
  candidateSummary: string | null;
  candidateSummarizedBy: string | null;
  candidateKeywords: string[];
  success: boolean;
  errorMessage: string | null;
}

export interface AdminArticleResummaryPreviewResponse {
  items: AdminArticleResummaryPreviewItemResponse[];
}

export interface AdminArticleResummaryApplyItemResponse {
  articleSummaryId: number;
  articleId: number | null;
  applied: boolean;
  summaryApplied: boolean;
  keywordsApplied: boolean;
  errorMessage: string | null;
}

export interface AdminArticleResummaryApplyResponse {
  items: AdminArticleResummaryApplyItemResponse[];
}

export interface AdminCssSelectorTestMatchResponse {
  title: string;
  url: string;
}

export interface AdminCssSelectorTestResponse {
  success: boolean;
  blogUrl: string;
  cssSelector: string;
  matchedElementCount: number;
  extractableUrlCount: number;
  sampleMatches: AdminCssSelectorTestMatchResponse[];
  message: string | null;
}

export interface AdminCssSelectorCandidateResponse {
  selector: string;
  confidence: number;
  baseConfidence: number;
  hintScore: number;
  firstArticleTitleMatched: boolean;
  firstArticleUrlMatched: boolean;
  matchedElementCount: number;
  extractableUrlCount: number;
  internalUrlCount: number;
  sampleMatches: AdminCssSelectorTestMatchResponse[];
}

export interface AdminCssSelectorSuggestResponse {
  success: boolean;
  blogUrl: string;
  candidates: AdminCssSelectorCandidateResponse[];
  message: string | null;
}

export interface AdminBlogRequiringSelectorResponse {
  id: number;
  name: string;
  url: string;
  rssStatus: "NO_RSS" | "WITH_CONTENT" | "WITHOUT_CONTENT";
  createdAt: string;
}

export interface AdminBlogCssSelectorUpdateResponse {
  blogId: number;
  blogUrl: string;
  cssSelector: string;
  saved: boolean;
  testResult: AdminCssSelectorTestResponse;
  message: string | null;
}

export type AdminArticleReportStatus =
  | "RECEIVED"
  | "REVIEWED"
  | "RESOLVED"
  | "DISMISSED";

export interface AdminRecentArticleResponse {
  id: number;
  blogId: number;
  blogName: string;
  blogUrl: string;
  blogImage: string | null;
  title: string;
  url: string;
  isVisible: boolean;
  summary: string | null;
  summarizedBy: string | null;
  createdDate: string;
  reportCount: number;
  receivedReportCount: number;
}

export interface AdminArticleVisibilityUpdateResponse {
  articleId: number;
  beforeIsVisible: boolean;
  isVisible: boolean;
}

export interface AdminArticleReportResponse {
  id: number;
  articleId: number;
  articleTitle: string;
  articleUrl: string;
  blogId: number;
  blogName: string;
  userId: number;
  reason: string;
  detail: string | null;
  status: AdminArticleReportStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AdminBlogResponse {
  id: number;
  name: string;
  url: string;
  image: string | null;
  rss: string | null;
  urlCssSelector: string | null;
  rssStatus: "NO_RSS" | "WITH_CONTENT" | "WITHOUT_CONTENT";
  isShowOnMain: boolean;
  createdBy: number;
  createdAt: string;
}

export interface AdminKeywordHeadUpdateResponse {
  keywordId: number;
  beforeHeadKeywordId: number | null;
  afterHeadKeywordId: number | null;
}

export interface AdminKeywordCreateResponse {
  id: number;
  value: string;
  isVisible: boolean;
  isHead: boolean;
}
