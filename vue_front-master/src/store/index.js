import Vue from "vue";
import Vuex from "vuex";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    //baseURL:  "http://localhost:8090",
    baseURL: "https://mosaicpatientreporttools.com",
    //analysisUrl: "http://172.26.88.0:5000",
    analysisUrl: "http://127.0.0.1:4523/m1/5573315-5251000-default",
    //analysisUrl: "http://3.92.49.119:5000",
  },
  getters: {
    baseURL: state => state.baseURL
  },
  mutations: {},
  actions: {},
  modules: {},
});
