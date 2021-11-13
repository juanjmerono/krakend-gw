module.exports = {
  transpileDependencies: ['vue-oidc-client'],
  chainWebpack: config => {
    // config.resolve.set('symlinks', false)
    config.devServer.host('0.0.0.0').port(8080).disableHostCheck(true)
  }
}
