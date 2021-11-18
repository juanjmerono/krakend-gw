<template>
  <div id="app">
    <div id="nav">
      <a href="http://apidocs.umes/">Go to apidocs</a> |
      <a href="http://rrhh.umes/rrhh">Go to classic app</a> |
      <a href="http://pkce.umes/">Go to modern app</a> |
      <router-link to="/">Home</router-link> |
      <router-link to="/about">About (Protected) </router-link> |
      <a href="#"
         @click="$oidc.signOut()"
         v-if="$oidc.isAuthenticated">Signout</a>
    </div>
    <router-view />
  </div>
</template>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
