FROM postgres:17

# Installa pacchetti PostGIS
RUN apt-get update \
  && apt-get install -y postgis postgresql-17-postgis-3 \
  && rm -rf /var/lib/apt/lists/*