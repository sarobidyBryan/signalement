FROM node:20 AS build
WORKDIR /app

# Build-time API URL (Vite reads VITE_* env vars at build-time)
ARG VITE_API_URL=http://backend:8080
ENV VITE_API_URL=$VITE_API_URL

# Copy package metadata first to install dependencies inside container
# This ensures node_modules are created inside the image (as requested).
COPY package*.json ./

# If the project provides a lockfile, prefer deterministic install using npm ci.
# Otherwise fallback to npm install so the image still builds when you haven't
# committed a lockfile yet.
RUN if [ -f package-lock.json ]; then \
			# Install dev dependencies too because build tools like Vite are devDeps
			npm ci --no-audit --no-fund; \
		else \
			npm install --no-audit --no-fund; \
		fi

# Copy source and run the production build
COPY . .
RUN npm run build

FROM nginx:alpine AS runtime
# Serve the static build
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
