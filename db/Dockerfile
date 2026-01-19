FROM postgres:15
ENV DEBIAN_FRONTEND=noninteractive

# Install PostGIS packages
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql-15-postgis-3 \
        postgresql-15-postgis-3-scripts \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY initdb /docker-entrypoint-initdb.d/
