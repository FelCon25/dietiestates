-- Create spatial index for properties
CREATE INDEX idx_properties_location ON properties USING GIST (
  ST_Point(CAST(longitude AS FLOAT), CAST(latitude AS FLOAT))::geography
);
