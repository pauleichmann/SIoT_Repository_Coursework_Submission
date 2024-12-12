import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
// Import Element UI component library
import ElementUI from 'element-ui'

// Load Element UI component library styles
import 'element-ui/lib/theme-chalk/index.css'
import enLocale from 'element-ui/lib/locale/lang/en';

// Register Element UI component library globally
Vue.use(ElementUI, { locale: enLocale })

Vue.config.productionTip = false;

const app = new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
export default app;