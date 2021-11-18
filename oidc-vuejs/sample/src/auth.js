import Vue from 'vue'
import { createOidcAuth, SignInType, LogLevel } from 'vue-oidc-client'

const loco = window.location
const appRootUrl = `${loco.protocol}//${loco.host}${process.env.BASE_URL}`

var mainOidc = createOidcAuth(
  'main',
  SignInType.Popup,
  appRootUrl,
  {
    authority: 'https://8463-31-222-83-252.ngrok.io/cas/oidc/',
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
