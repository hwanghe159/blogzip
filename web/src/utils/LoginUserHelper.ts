interface LoginUser {
  id: number;
  accessToken: string;
  email: string;
  image: string;
}

const localStorageKey = 'loginUser';

export const isLogined = (): boolean => {
  const loginUser = getLoginUser()
  return loginUser !== null && loginUser.accessToken.trim() !== '';
};

// 로그인 사용자 정보를 가져오는 함수
export const getLoginUser = (): LoginUser | null => {
  const storedUser = localStorage.getItem(localStorageKey);
  if (storedUser) {
    return JSON.parse(storedUser);
  }
  return null;
};

// 로그인 사용자 정보를 저장하는 함수
export const setLoginUser = (user: LoginUser): void => {
  localStorage.setItem(localStorageKey, JSON.stringify(user));
};

// 로그인 사용자 정보를 수정하는 함수
export const updateLoginUser = (updatedFields: Partial<LoginUser>): void => {
  const storedUser = getLoginUser();
  if (storedUser) {
    const updatedUser = {...storedUser, ...updatedFields};
    localStorage.setItem(localStorageKey, JSON.stringify(updatedUser));
  }
};

// 로그인 사용자 정보를 제거하는 함수
export const removeLoginUser = (): void => {
  localStorage.removeItem(localStorageKey);
};