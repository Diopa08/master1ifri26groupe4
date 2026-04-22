# ── Étape 1 : Compilation du code source ──────────────────────────────────────
FROM node:18-alpine AS build
WORKDIR /app

# Installer les dépendances (layer mis en cache si package.json n'a pas changé)
COPY package*.json ./
RUN npm install

# Copier le code source et compiler
COPY . .
RUN npm run build

# ── Étape 2 : Serveur Nginx avec le build final ────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
