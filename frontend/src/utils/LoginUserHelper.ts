export interface LoginUser {
  id: number;
  accessToken: string;
  email: string;
  image: string;
}

const localStorageKey = 'loginUser';
const loginUserChangeEvent = 'login-user-changed';

const dispatchLoginUserChanged = (): void => {
  window.dispatchEvent(new Event(loginUserChangeEvent));
};

export const subscribeLoginUserChange = (listener: () => void): (() => void) => {
  window.addEventListener(loginUserChangeEvent, listener);
  window.addEventListener('storage', listener);

  return () => {
    window.removeEventListener(loginUserChangeEvent, listener);
    window.removeEventListener('storage', listener);
  };
};

export const isLogined = (): boolean => {
  const loginUser = getLoginUser();
  return loginUser !== null && loginUser.accessToken.trim() !== '';
};

export const getLoginUser = (): LoginUser | null => {
  const storedUser = localStorage.getItem(localStorageKey);
  if (!storedUser) {
    return null;
  }

  return JSON.parse(storedUser) as LoginUser;
};

export const setLoginUser = (user: LoginUser): void => {
  localStorage.setItem(localStorageKey, JSON.stringify(user));
  dispatchLoginUserChanged();
};

export const updateLoginUser = (updatedFields: Partial<LoginUser>): void => {
  const storedUser = getLoginUser();
  if (!storedUser) {
    return;
  }

  const updatedUser = { ...storedUser, ...updatedFields };
  localStorage.setItem(localStorageKey, JSON.stringify(updatedUser));
  dispatchLoginUserChanged();
};

export const removeLoginUser = (): void => {
  localStorage.removeItem(localStorageKey);
  dispatchLoginUserChanged();
};
