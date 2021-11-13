# krakend-gw

This is a POC for different types of applications.

## Public Clients

Un cliente público es aquel que requiere autenticarse pero se ejecuta en un dominio NO protegido, como una SPA, una aplicación de escritorio o móvil, cualquiera en la que personas externas puedan acceder o decompilar su código.

En estos casos NO es posible emplear esquemas con secreto compartido, como Authentication Code, y para eso se creó el flujo Authentication Code with Proof Key of Code Exchange (PKCE).

## Confidential Clients

Son clientes protegidos, como las aplicaciones web (backend) de las que, en principio, no es posible obtener el código fuente y por tanto es securo alojar un secreto compartido con el servidor de autenticación.

Aunque el flujo original Authorization Code es el usado para estos caso, recientemente se recomienda utilizar PKCE para todo tipo de cliente, en OAuth 2.1 será requerido para todo tipo de clientes.

## OpenID Connect vs OAuth

OpenID Connect es una capa encima de OAuth que proporciona autenticación (una prueba de que el usuario se ha autenticado) y permite disponer a la aplicación información sobre el usuario autenticado mediante un ID Token que es diferente de un Access Token que es lo que OAuth proporciona.

### ID Token vs Access Token

Se explica estupendamente [aqui](https://auth0.com/blog/id-token-access-token-what-is-the-difference/), pero el resumen es:

ID Token: Es un token en formato JWT que contiene información del usuario, es decir la aplicación que lo obtenga no tiene porqué hacer peticiones al servidor para obtener el nombre del usuario conectado, pero no contiene información de autorización, por lo que no debe usarse para invocar APIs.

Access Token: Es un token que puede estar en cualquier formato, aunque suele ser JWT, y que contiene información de autorización y se usa para invocar APIs.

### Flujo correcto

El flujo correcto de PKCE debe ser obtener un ID Token y un Access Token para luego utilizar el Access Token en las llamadas a las APIs que proporcionen recursos. El ejemplo en vivo [aqui](https://okta-oidc-fun.herokuapp.com/)

Usar en confidential client PKCE [aqui](https://dzone.com/articles/securing-web-apps-using-pkce-with-spring-boot)

VUEJS con Flujo PKCE [aqui](https://fawnoos.com/2020/12/20/cas63x-oidc-spa-vue-app/)

## Demo

La demo contiene:

- Una instancia de identity provider en este caso CAS 6.3.7
- 
