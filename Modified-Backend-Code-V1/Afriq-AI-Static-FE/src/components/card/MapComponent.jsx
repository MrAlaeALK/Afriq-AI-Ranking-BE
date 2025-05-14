import React, { useEffect, useState, useRef, useMemo } from 'react';
import { MapContainer, TileLayer, GeoJSON, Popup, ZoomControl } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const MapComponent = ({ selectedCountry, colorScale, weights }) => {
  const [geojsonData, setGeojsonData] = useState(null);
  const [error, setError] = useState(null);
  const [countryScores, setCountryScores] = useState([]);
  const [selectedCountryData, setSelectedCountryData] = useState(null);
  const [popupPosition, setPopupPosition] = useState(null);
  const mapRef = useRef(null);

  // Demo country data - in a real app, this would come from an API
  const demoData = [
    { id: 1, name: 'Rwanda', flag: '🇷🇼', region: 'East Africa',
      scores: { global: 82, odin: 85, hdi: 76, internet: 84, education: 70, gdp: 65, innovation: 68, governance: 78, health: 72, environment: 69 }},
    { id: 2, name: 'Mauritius', flag: '🇲🇺', region: 'East Africa',
      scores: { global: 79, odin: 80, hdi: 88, internet: 70, education: 85, gdp: 82, innovation: 75, governance: 80, health: 84, environment: 77 }},
    { id: 3, name: 'South Africa', flag: '🇿🇦', region: 'Southern Africa',
      scores: { global: 76, odin: 78, hdi: 83, internet: 67, education: 79, gdp: 76, innovation: 82, governance: 72, health: 68, environment: 65 }},
    { id: 4, name: 'Kenya', flag: '🇰🇪', region: 'East Africa',
      scores: { global: 74, odin: 76, hdi: 70, internet: 75, education: 72, gdp: 68, innovation: 77, governance: 73, health: 70, environment: 72 }},
    { id: 5, name: 'Morocco', flag: '🇲🇦', region: 'North Africa',
      scores: { global: 72, odin: 68, hdi: 80, internet: 72, education: 75, gdp: 73, innovation: 70, governance: 68, health: 76, environment: 71 }},
    { id: 6, name: 'Ghana', flag: '🇬🇭', region: 'West Africa',
      scores: { global: 70, odin: 72, hdi: 65, internet: 73, education: 68, gdp: 67, innovation: 69, governance: 75, health: 64, environment: 68 }},
    { id: 7, name: 'Tunisia', flag: '🇹🇳', region: 'North Africa',
      scores: { global: 69, odin: 65, hdi: 79, internet: 68, education: 77, gdp: 71, innovation: 67, governance: 64, health: 75, environment: 70 }},
    { id: 8, name: 'Senegal', flag: '🇸🇳', region: 'West Africa',
      scores: { global: 67, odin: 70, hdi: 60, internet: 69, education: 63, gdp: 59, innovation: 66, governance: 71, health: 62, environment: 67 }},
    { id: 9, name: 'Egypt', flag: '🇪🇬', region: 'North Africa',
      scores: { global: 65, odin: 62, hdi: 73, internet: 64, education: 70, gdp: 68, innovation: 61, governance: 60, health: 72, environment: 59 }},
    { id: 10, name: 'Ivory Coast', flag: '🇨🇮', region: 'West Africa',
      scores: { global: 63, odin: 66, hdi: 55, internet: 67, education: 58, gdp: 62, innovation: 64, governance: 59, health: 57, environment: 63 }},
    { id: 11, name: 'Nigeria', flag: '🇳🇬', region: 'West Africa',
      scores: { global: 61, odin: 63, hdi: 59, internet: 60, education: 61, gdp: 65, innovation: 62, governance: 57, health: 58, environment: 56 }},
    { id: 12, name: 'Tanzania', flag: '🇹🇿', region: 'East Africa',
      scores: { global: 58, odin: 60, hdi: 54, internet: 59, education: 55, gdp: 53, innovation: 57, governance: 61, health: 56, environment: 62 }},
    { id: 13, name: 'Ethiopia', flag: '🇪🇹', region: 'East Africa',
      scores: { global: 55, odin: 57, hdi: 49, internet: 58, education: 51, gdp: 48, innovation: 54, governance: 56, health: 50, environment: 59 }},
    { id: 14, name: 'Angola', flag: '🇦🇴', region: 'Southern Africa',
      scores: { global: 52, odin: 53, hdi: 58, internet: 46, education: 50, gdp: 60, innovation: 47, governance: 45, health: 54, environment: 51 }},
    { id: 15, name: 'Mozambique', flag: '🇲🇿', region: 'Southern Africa',
      scores: { global: 49, odin: 50, hdi: 45, internet: 51, education: 46, gdp: 44, innovation: 48, governance: 52, health: 47, environment: 53 }},
  ];

  // Fonction pour calculer le score pondéré d'un pays
  const calculateWeightedScore = (countryScores, weights) => {
    if (!weights || Object.keys(weights).length === 0) {
      return countryScores?.global || 0;
    }
    
    let weightedScore = 0;
    let totalWeight = 0;
    
    // Calculer le score pondéré en multipliant chaque critère par son poids
    Object.keys(weights).forEach(criteria => {
      if (countryScores?.[criteria] !== undefined) {
        weightedScore += (countryScores[criteria] * weights[criteria]);
        totalWeight += weights[criteria];
      }
    });
    
    // Normaliser le score sur 100 si des poids ont été appliqués
    return totalWeight > 0 ? weightedScore / totalWeight : countryScores?.global || 0;
  };

  // Sort countries by weighted scores
  const sortedCountries = useMemo(() => {
    return [...demoData].sort((a, b) => {
      const scoreA = calculateWeightedScore(a.scores, weights);
      const scoreB = calculateWeightedScore(b.scores, weights);
      return scoreB - scoreA;
    });
  }, [weights]);
  
  // Apply ranking based on sorted scores
  const rankedCountries = useMemo(() => {
    return sortedCountries.map((country, index) => ({
      ...country,
      rank: index + 1,
      weightedScore: calculateWeightedScore(country.scores, weights)
    }));
  }, [sortedCountries, weights]);

  useEffect(() => {
    setCountryScores(rankedCountries);
  }, [rankedCountries]);

  useEffect(() => {
    fetch('/geojson/africa_map.json')
      .then(response => {
        if (!response.ok) throw new Error(`Error loading GeoJSON: ${response.status}`);
        return response.json();
      })
      .then(data => setGeojsonData(data))
      .catch(err => {
        console.error("Error loading GeoJSON:", err);
        setError(err.message);
      });
  }, []);

  // Find centroid of a country by name
  const findCountryCentroid = (countryName) => {
    if (!geojsonData) return null;
    
    const feature = geojsonData.features.find(f => {
      const name = f.properties?.name || 
                   f.properties?.NAME || 
                   f.properties?.SOVEREIGNT || 
                   f.properties?.merge_group;
      return name === countryName;
    });
    
    if (feature && feature.geometry) {
      // For Polygon
      if (feature.geometry.type === 'Polygon') {
        const coords = feature.geometry.coordinates[0];
        let lat = 0, lng = 0;
        coords.forEach(coord => {
          lat += coord[1];
          lng += coord[0];
        });
        return [lat / coords.length, lng / coords.length];
      }
      // For MultiPolygon
      else if (feature.geometry.type === 'MultiPolygon') {
        // Use the first polygon for simplicity
        const coords = feature.geometry.coordinates[0][0];
        let lat = 0, lng = 0;
        coords.forEach(coord => {
          lat += coord[1];
          lng += coord[0];
        });
        return [lat / coords.length, lng / coords.length];
      }
    }
    return null;
  };

  // Effect to handle selected country changes from dropdown
  useEffect(() => {
    if (selectedCountry && geojsonData) {
      // Utiliser les données les plus récentes (avec le rank et weightedScore actualisés)
      const countryData = rankedCountries.find(c => c.name === selectedCountry);
      if (countryData) {
        const centroid = findCountryCentroid(selectedCountry);
        if (centroid) {
          setSelectedCountryData(countryData);
          setPopupPosition({ lat: centroid[0], lng: centroid[1] });
          
          // Zoom to the country if we have a ref to the map
          if (mapRef.current) {
            mapRef.current.flyTo(centroid, 4);
          }
        }
      }
    }
  }, [selectedCountry, rankedCountries, geojsonData]);

  // Get color based on score
  const getColorByScore = (score) => {
    if (score === undefined) return '#cccccc'; // Default gray for unknown scores
    
    if (colorScale === 'green-red') {
      if (score >= 80) return '#109618'; // Dark green
      if (score >= 60) return '#7fbd5a'; // Light green
      if (score >= 40) return '#ffeb3b'; // Yellow
      if (score >= 20) return '#f44336'; // Red
      return '#b71c1c'; // Dark red
    } else if (colorScale === 'blue-red') {
      if (score >= 80) return '#1a237e'; // Dark blue
      if (score >= 60) return '#3f51b5'; // Blue
      if (score >= 40) return '#9c27b0'; // Purple
      if (score >= 20) return '#e91e63'; // Pink
      return '#b71c1c'; // Dark red
    } else if (colorScale === 'purple-yellow') {
      if (score >= 80) return '#4a148c'; // Dark purple
      if (score >= 60) return '#9c27b0'; // Purple
      if (score >= 40) return '#e91e63'; // Pink
      if (score >= 20) return '#ff9800'; // Orange
      return '#ffeb3b'; // Yellow
    }
    
    return '#cccccc'; // Default gray
  };

  const getGeoJSONStyle = (feature) => {
    const countryName = 
      feature.properties?.name || 
      feature.properties?.NAME || 
      feature.properties?.SOVEREIGNT || 
      feature.properties?.merge_group || 
      'Unknown country';
    
    // Find country data from rankedCountries to get the most up-to-date data
    const countryData = rankedCountries.find(c => c.name === countryName);
    
    // Utiliser directement weightedScore s'il existe, sinon calculer
    const weightedScore = countryData ? 
      (countryData.weightedScore || calculateWeightedScore(countryData.scores, weights)) : 
      undefined;
    
    if ((selectedCountryData && countryName === selectedCountryData.name) || (selectedCountry && countryName === selectedCountry)) {
      return {
        fillColor: '#ff6b6b',
        weight: 2,
        color: '#ff0000',
        fillOpacity: 1,
      };
    }
    
    return {
      fillColor: weightedScore !== undefined ? getColorByScore(weightedScore) : '#1a1d62',
      weight: 1,
      opacity: 1,
      color: '#4a83ec',
      fillOpacity: 0.8,
    };
  };

  const onEachFeature = (feature, layer) => {
    const countryName = 
      feature.properties?.name || 
      feature.properties?.NAME || 
      feature.properties?.SOVEREIGNT || 
      feature.properties?.merge_group || 
      'Unknown country';
    
    layer.on({
      mouseover: (e) => {
        const layer = e.target;
        layer.setStyle({
          fillOpacity: 1,
          weight: 2,
        });
        layer.bringToFront();
      },
      mouseout: (e) => {
        const layer = e.target;
        
        // Utiliser la fonction getGeoJSONStyle pour avoir le style cohérent avec colorScale
        layer.setStyle(getGeoJSONStyle(feature));
      },
      click: (e) => {
        console.log("Country clicked:", countryName);
        
        // Utiliser rankedCountries pour avoir les données les plus récentes
        const countryData = rankedCountries.find(c => c.name === countryName);
        
        if (countryData) {
          setSelectedCountryData(countryData);
          setPopupPosition(e.latlng);
        } else {
          console.log("No data for this country");
        }
      },
    });
  };

  const closePopup = () => {
    setSelectedCountryData(null);
    setPopupPosition(null);
  };

  // Create score badge with color
  const ScoreBadge = ({ score }) => {
    let color;
    if (score >= 80) color = 'bg-green-100 text-green-800';
    else if (score >= 60) color = 'bg-blue-100 text-blue-800';
    else if (score >= 40) color = 'bg-yellow-100 text-yellow-800';
    else if (score >= 20) color = 'bg-orange-100 text-orange-800';
    else color = 'bg-red-100 text-red-800';
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${color}`}>
        {score}
      </span>
    );
  };

  // Country popup content
  const CountryPopup = ({ country, weights }) => {
    if (!country) return null;
    
    // Display only criteria that are included in the weights object
    const criteriaTitles = {
      odin: 'ODIN Index (Open Data)',
      hdi: 'Human Development Index',
      internet: 'Internet Access',
      education: 'Education',
      gdp: 'GDP per Capita',
      innovation: 'Innovation',
      governance: 'Governance',
      health: 'Health',
      environment: 'Environment'
    };

    // Toujours recalculer le score pondéré pour s'assurer qu'il utilise les poids actuels
    // Cela garantit que même si le composant ne se re-render pas,
    // nous avons le bon score affiché
    const globScore = Number(calculateWeightedScore(country.scores, weights).toFixed(1));
    
    // Trouver le pays avec les données mises à jour dans rankedCountries
    const updatedCountry = rankedCountries.find(c => c.name === country.name);
    const currentRank = updatedCountry ? updatedCountry.rank : country.rank;
    
    return (
      <div className="country-popup" style={{ minWidth: '300px', maxWidth: '400px' }}>
        <div className="flex justify-between items-center mb-2">
          <div className="flex items-center">
            <span className="text-2xl mr-2">{country.flag}</span>
            <h3 className="text-xl font-bold">{country.name}</h3>
          </div>
          <div className="bg-gray-100 rounded-full px-3 py-1 text-sm">
            Rank: #{currentRank}
          </div>
        </div>

        <div className="mb-4">
          <div className="flex justify-between items-center">
            <div className="text-gray-600">Global Score:</div>
            <div className="flex items-center">
              <div className="w-32 bg-gray-200 rounded-full h-2.5 mr-2">
                <div className="bg-blue-600 h-2.5 rounded-full" style={{ width: `${globScore}%` }}></div>
              </div>
              <ScoreBadge score={globScore} />
            </div>
          </div>
        </div>
        
        <hr className="my-3" />
        
        <div className="space-y-2">
          <h4 className="font-medium">Score Details</h4>
          {weights && Object.keys(weights).map(criteria => (
            criteriaTitles[criteria] && (
              <div key={criteria} className="flex justify-between items-center">
                <div className="text-sm text-gray-600">{criteriaTitles[criteria]} :</div>
                <div className="flex items-center">
                  <div className="w-24 bg-gray-200 rounded-full h-2 mr-2">
                    <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${country.scores[criteria]}%` }}></div>
                  </div>
                  <ScoreBadge score={country.scores[criteria]} />
                </div>
              </div>
            )
          ))}
        </div>
        
        <div className="mt-4 pt-3 border-t border-gray-200">
          <div className="text-xs text-gray-500">
            Region: {country.region}
          </div>
        </div>
        
        <button 
          onClick={closePopup}
          className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    );
  };

  if (error) {
    return <div className="text-red-500 p-5">Error: {error}</div>;
  }

  return (
    <div className="map-container relative z-0" style={{
      height: '400px',
      width: '100%',
      margin: '0 auto',
      position: 'relative',
      borderRadius: '8px',
      overflow: 'hidden'
    }}>
      <MapContainer
        center={[5, 20]}
        zoom={2}
        style={{ width: '100%', height: '100%' }}
        zoomControl={false}
        minZoom={2}
        maxZoom={4}
        maxBounds={[[-40, -40], [40, 60]]}
        ref={mapRef}
        whenCreated={(map) => { mapRef.current = map; }}
      >
        <ZoomControl position="bottomright" />
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="&copy; OpenStreetMap"
        />
        {geojsonData && (
          <GeoJSON
            data={geojsonData}
            onEachFeature={onEachFeature}
            style={getGeoJSONStyle}
          />
        )}
        
        {popupPosition && selectedCountryData && (
          <Popup 
            position={popupPosition}
            maxWidth={400}
            offset={[0, -10]}
            autoPan={true}
            closeButton={false}
          >
            <CountryPopup country={selectedCountryData} weights={weights} />
          </Popup>
        )}
      </MapContainer>
    </div>
  );
};

export default MapComponent;