"use client";

import { useEffect } from "react";
import { MapContainer, TileLayer, Marker, GeoJSON, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { SiteAnalysisResponse } from "@/lib/types";

// Leaflet's default marker icons reference image files via paths that
// break under Next.js's bundler unless reconfigured — this is the
// standard, documented fix (not a hack), pointing at unpkg-hosted assets
// instead of trying to bundle Leaflet's images locally.
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

function RecenterOnChange({ lat, lng }: { lat: number; lng: number }) {
  const map = useMap();
  useEffect(() => { map.setView([lat, lng], 17); }, [lat, lng, map]);
  return null;
}

export function LeafletMap({ site }: { site: SiteAnalysisResponse }) {
  if (!site.latitude || !site.longitude) {
    return (
      <div className="flex h-full items-center justify-center text-sm text-white/40">
        No location analyzed yet.
      </div>
    );
  }

  const position: [number, number] = [site.latitude, site.longitude];
  const roadsGeojson = site.nearbyRoadsGeojson ? JSON.parse(site.nearbyRoadsGeojson) : null;

  return (
    <MapContainer center={position} zoom={17} scrollWheelZoom className="h-full w-full">
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <RecenterOnChange lat={position[0]} lng={position[1]} />

      <Marker position={position}>
        <Popup>
          <div className="text-xs">
            <strong>Plot location</strong>
            <br />
            {site.geocodedAddress}
          </div>
        </Popup>
      </Marker>

      {roadsGeojson && (
        <GeoJSON
          data={roadsGeojson}
          style={(feature) => ({
            color: feature?.properties?.width ? "#22d3ee" : "#f59e0b",
            weight: 3,
            opacity: 0.8,
          })}
          onEachFeature={(feature, layer) => {
            const { highway, name, width } = feature.properties || {};
            layer.bindPopup(
              `<div style="font-size:12px">
                 <strong>${name || "Unnamed road"}</strong><br/>
                 Type: ${highway}<br/>
                 Width: ${width ? `${width}m (tagged)` : "not tagged — estimated"}
               </div>`
            );
          }}
        />
      )}
    </MapContainer>
  );
}
