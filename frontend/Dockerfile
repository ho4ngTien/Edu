# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app

# Copy package descriptors first to leverage Docker layer caching
COPY package*.json ./
RUN npm ci

# Copy the rest of the source code and build the production distribution
COPY . .
RUN npm run build

# Stage 2: Serve the production bundle using Nginx
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# Remove default Nginx static files
RUN rm -rf ./*

# Copy built frontend assets from the build stage
COPY --from=build /app/dist .

# Copy custom Nginx configuration for routing and reverse proxying
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
