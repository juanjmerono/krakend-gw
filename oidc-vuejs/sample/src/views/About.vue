<template>
  <div class="about">
    <h1>This is a login-protected page</h1>
    <ul class="claims">
      <li v-for="c in claims"
          :key="c.key">
        <strong>{{c.key}}</strong>: {{c.value}}</li>
    </ul>
    <input v-model="apipath" placeholder="/resources/hello"><br/>
    <button v-on:click="callAPI(0)">No Usar Token</button>
    <button v-on:click="callAPI(1)">Usar Token Incorrecto</button>
    <button v-on:click="callAPI(2)">Usar Token Correcto</button>
    <p>{{ saludo }}</p>
  </div>
</template>
<script>
export default {
  data: function() {
    return { 
      saludo: "Haz click para invocar a la API",
      apipath: "/oauth/api1/hello"
    }
  },
  methods: {
    callAPI: function(useToken) {
      let xhr = new XMLHttpRequest()
      xhr.open("GET", "http://api.umes"+this.apipath, true)
      xhr.setRequestHeader('Content-Type', 'application/json')
      const accessToken = this.user['access_token']
      if (useToken && accessToken) {
        xhr.setRequestHeader('Authorization', 'Bearer '+ accessToken+(useToken==1?'xxxx':''))
      }
      xhr.onload = () => {
        if (xhr.status === 200) {
          this.saludo = xhr.responseText || "???";
        } else {
          this.saludo = "Ha fallado la llamada a la API HTTP Status: "+xhr.status;
        }
      }
      xhr.send()
    }
  },
  computed: {
    user() {
      return this.$oidc.user;
    },
    claims() {
      if (this.user) {
        return Object.keys(this.user).map(key => ({
          key,
          value: this.user[key]
        }));
      }
      return [];
    }
  }
};
</script>
<style>
.claims {
  list-style: none;
  text-align: left;
}
</style>
