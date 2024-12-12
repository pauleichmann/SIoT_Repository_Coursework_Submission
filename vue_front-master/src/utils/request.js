// src/utils/axiosInstance.js
import axios from "axios";

// create Axios instance
const axiosInstance = axios.create({
  //baseURL: "https://localhost:8080", // your API fundemental URL
  timeout: 10000, // request for overtime
  headers: { "Content-Type": "application/json" },
});

// add request interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    // Perform actions before sending a request, such as adding authentication token
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // what to do with request error
    return Promise.reject(error);
  }
);

// add response interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    // what to do tiwht response data
    return response.data;
  },
  (error) => {
    // what to do with response error
    if (error.response) {
      // request sent，but server response status is not in the range of 2xx
      switch (error.response.status) {
        case 401:
          // not authorized, redirect to login page
          console.error("Not authorized, Please Log in again");
          break;
        case 403:
          // forbit access
          console.error("access forbidden");
          break;
        case 404:
          // resources not found
          console.error("resources not found");
          break;
        // deal with other errors
        default:
          console.error("other error:", error.response.data);
      }
    } else if (error.request) {
      // request sent, but no response
      console.error("no repsonse:", error.request);
    } else {
      // other error
      console.error("other error:", error.message);
    }
    return Promise.reject(error);
  }
);

// export GET and POST functions
export const get = (url, params = {}) => {
  return axiosInstance.get(url, { params });
};

export const post = (url, data = {}) => {
  return axiosInstance.post(url, data);
};

