import axios, {AxiosRequestConfig, AxiosResponse} from 'axios';
import {ErrorResponse} from "./ErrorResponse";

export class Api {
  private onSuccessHandler: (response: AxiosResponse) => void = () => {
  };
  private on4XXHandler: (error: ErrorResponse) => void = () => {
  };
  private on5XXHandler: (error: ErrorResponse) => void = () => {
  };

  static get(url: string, config?: AxiosRequestConfig) {
    const instance = new Api();
    axios.get(url, config)
    .then(instance.handleSuccess)
    .catch(instance.handleError);
    return instance;
  }

  static post(url: string, data: any, config?: AxiosRequestConfig) {
    const instance = new Api();
    axios.post(url, data, config)
    .then(instance.handleSuccess)
    .catch(instance.handleError);
    return instance;
  }

  static put(url: string, data: any, config?: AxiosRequestConfig) {
    const instance = new Api();
    axios.put(url, data, config)
    .then(instance.handleSuccess)
    .catch(instance.handleError);
    return instance;
  }

  static patch(url: string, data: any, config?: AxiosRequestConfig) {
    const instance = new Api();
    axios.patch(url, data, config)
    .then(instance.handleSuccess)
    .catch(instance.handleError);
    return instance;
  }

  static delete(url: string, config?: AxiosRequestConfig) {
    const instance = new Api();
    axios.delete(url, config)
    .then(instance.handleSuccess)
    .catch(instance.handleError);
    return instance;
  }

  onSuccess(handler: (response: AxiosResponse) => void) {
    this.onSuccessHandler = handler;
    return this;
  }

  on4XX(handler: (error: ErrorResponse) => void) {
    this.on4XXHandler = handler;
    return this;
  }

  on5XX(handler: (error: ErrorResponse) => void) {
    this.on5XXHandler = handler;
    return this;
  }

  private handleSuccess = (response: AxiosResponse) => {
    this.onSuccessHandler(response);
  }

  private handleError = (error: any) => {
    if (axios.isAxiosError(error) && error.response) {
      const status = error.response.status;
      const errorResponse: ErrorResponse = error.response.data;

      if (status >= 400 && status < 500) {
        this.on4XXHandler(errorResponse);
      } else if (status >= 500) {
        this.on5XXHandler(errorResponse);
      }
    } else {
      console.error("Unhandled error", error);
    }
  }
}