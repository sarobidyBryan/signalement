FROM postgres:15
ENV DEBIAN_FRONTEND=noninteractive

# Install PostGIS packages
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    postgis \
    postgresql-15-postgis-3 || true \
  && rm -rf /var/lib/apt/lists/*

COPY initdb /docker-entrypoint-initdb.d/
