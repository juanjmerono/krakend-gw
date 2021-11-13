FROM node:14.18.1-alpine

# instalar un simple servidor http para servir nuestro contenido estático
#RUN npm install -g yarn

# hacer la carpeta 'app' el directorio de trabajo actual
WORKDIR /app

# copiar 'package.json' y 'package-lock.json' (si están disponibles)
COPY package*.json ./
COPY yarn.lock ./

# instalar dependencias del proyecto
RUN yarn

COPY . .

WORKDIR /app/sample

RUN yarn

# construir aplicación para producción minificada
#RUN yarn build

EXPOSE 8080
CMD ["yarn", "serve"]


# production environment
# FROM nginx:1.16.0-alpine
# COPY --from=build /app/sample/dist /usr/share/nginx/html
# EXPOSE 80
# CMD ["nginx", "-g", "daemon off;"]
