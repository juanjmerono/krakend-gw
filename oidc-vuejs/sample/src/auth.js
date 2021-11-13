import Vue from 'vue'
import { createOidcAuth, SignInType, LogLevel } from 'vue-oidc-client'

const loco = window.location
const appRootUrl = `${loco.protocol}//${loco.host}${process.env.BASE_URL}`

var mainOidc = createOidcAuth(
  'main',
  SignInType.Popup,
  appRootUrl,
  {
    authority: 'http://kubernetes.docker.internal:8001/cas/oidc/',
    client_id: 'clientid', // 'implicit.shortlived',
    response_type: 'code',
    scope: 'openid',
    // test use
    prompt: 'login',
    login_hint: 'bob'
  },
  console,
  LogLevel.Debug
)
Vue.prototype.$oidc = mainOidc
export default mainOidc
