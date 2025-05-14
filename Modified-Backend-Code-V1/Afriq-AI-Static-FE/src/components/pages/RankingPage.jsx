import React, { useState, useEffect } from 'react';
import CountryTable from '../ranking/CountryTable';
import RankingCharts from '../ranking/RankingCharts';
import AdvancedFilters from '../ranking/AdvancedFilters';
import RegionalFilters from '../ranking/RegionalFilters';
import ExportOptions from '../ranking/ExportOptions';

const RankingPage = () => {
  // État pour les pays et les filtres
  const [countries, setCountries] = useState([]);
  const [filteredCountries, setFilteredCountries] = useState([]);
  const [selectedCountries, setSelectedCountries] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeRegion, setActiveRegion] = useState('all');
  const [sortConfig, setSortConfig] = useState({ key: 'rank', direction: 'ascending' });
  const [customWeights, setCustomWeights] = useState({
    odin: 40,
    hdi: 30, 
    internet: 30
  });

  // Données de démonstration
  useEffect(() => {
    // Simuler un appel API pour obtenir les données des pays
    const demoData = [
      { id: 1, rank: 1, name: 'Rwanda', flag: '🇷🇼', region: 'East Africa', 
        scores: { global: 82, odin: 85, hdi: 76, internet: 84, education: 70, gdp: 65, innovation: 68, governance: 78, health: 72, environment: 69 }},
      { id: 2, rank: 2, name: 'Maurice', flag: '🇲🇺', region: 'East Africa', 
        scores: { global: 79, odin: 80, hdi: 88, internet: 70, education: 85, gdp: 82, innovation: 75, governance: 80, health: 84, environment: 77 }},
      { id: 3, rank: 3, name: 'Afrique du Sud', flag: '🇿🇦', region: 'Southern Africa', 
        scores: { global: 76, odin: 78, hdi: 83, internet: 67, education: 79, gdp: 76, innovation: 82, governance: 72, health: 68, environment: 65 }},
      { id: 4, rank: 4, name: 'Kenya', flag: '🇰🇪', region: 'East Africa', 
        scores: { global: 74, odin: 76, hdi: 70, internet: 75, education: 72, gdp: 68, innovation: 77, governance: 73, health: 70, environment: 72 }},
      { id: 5, rank: 5, name: 'Maroc', flag: '🇲🇦', region: 'North Africa', 
        scores: { global: 72, odin: 68, hdi: 80, internet: 72, education: 75, gdp: 73, innovation: 70, governance: 68, health: 76, environment: 71 }},
      { id: 6, rank: 6, name: 'Ghana', flag: '🇬🇭', region: 'West Africa', 
        scores: { global: 70, odin: 72, hdi: 65, internet: 73, education: 68, gdp: 67, innovation: 69, governance: 75, health: 64, environment: 68 }},
      { id: 7, rank: 7, name: 'Tunisie', flag: '🇹🇳', region: 'North Africa', 
        scores: { global: 69, odin: 65, hdi: 79, internet: 68, education: 77, gdp: 71, innovation: 67, governance: 64, health: 75, environment: 70 }},
      { id: 8, rank: 8, name: 'Sénégal', flag: '🇸🇳', region: 'West Africa', 
        scores: { global: 67, odin: 70, hdi: 60, internet: 69, education: 63, gdp: 59, innovation: 66, governance: 71, health: 62, environment: 67 }},
      { id: 9, rank: 9, name: 'Égypte', flag: '🇪🇬', region: 'North Africa', 
        scores: { global: 65, odin: 62, hdi: 73, internet: 64, education: 70, gdp: 68, innovation: 61, governance: 60, health: 72, environment: 59 }},
      { id: 10, rank: 10, name: 'Côte d\'Ivoire', flag: '🇨🇮', region: 'West Africa', 
        scores: { global: 63, odin: 66, hdi: 55, internet: 67, education: 58, gdp: 62, innovation: 64, governance: 59, health: 57, environment: 63 }},
      { id: 11, rank: 11, name: 'Nigéria', flag: '🇳🇬', region: 'West Africa', 
        scores: { global: 61, odin: 63, hdi: 59, internet: 60, education: 61, gdp: 65, innovation: 62, governance: 57, health: 58, environment: 56 }},
      { id: 12, rank: 12, name: 'Tanzanie', flag: '🇹🇿', region: 'East Africa', 
        scores: { global: 58, odin: 60, hdi: 54, internet: 59, education: 55, gdp: 53, innovation: 57, governance: 61, health: 56, environment: 62 }},
      { id: 13, rank: 13, name: 'Éthiopie', flag: '🇪🇹', region: 'East Africa', 
        scores: { global: 55, odin: 57, hdi: 49, internet: 58, education: 51, gdp: 48, innovation: 54, governance: 56, health: 50, environment: 59 }},
      { id: 14, rank: 14, name: 'Angola', flag: '🇦🇴', region: 'Southern Africa', 
        scores: { global: 52, odin: 53, hdi: 58, internet: 46, education: 50, gdp: 60, innovation: 47, governance: 45, health: 54, environment: 51 }},
      { id: 15, rank: 15, name: 'Mozambique', flag: '🇲🇿', region: 'Southern Africa', 
        scores: { global: 49, odin: 50, hdi: 45, internet: 51, education: 46, gdp: 44, innovation: 48, governance: 52, health: 47, environment: 53 }},
    ];

    setCountries(demoData);
    setFilteredCountries(demoData);
  }, []);

  // Fonctions de filtrage et de tri
  useEffect(() => {
    let result = [...countries];
    
    // Filtrage par région
    if (activeRegion !== 'all') {
      result = result.filter(country => country.region === activeRegion);
    }
    
    // Filtrage par recherche
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(country => 
        country.name.toLowerCase().includes(query)
      );
    }
    
    // Application de la pondération personnalisée
    result = result.map(country => {
      // Calculer le score pondéré basé sur tous les critères sélectionnés
      let weightedScore = 0;
      let totalWeight = 0;
      
      Object.entries(customWeights).forEach(([criterion, weight]) => {
        if (country.scores[criterion] !== undefined) {
          weightedScore += country.scores[criterion] * weight / 100;
          totalWeight += weight;
        }
      });
      
      // Normaliser au cas où tous les poids ne totalisent pas 100%
      if (totalWeight > 0 && totalWeight !== 100) {
        weightedScore = (weightedScore * 100) / totalWeight;
      }
      
      return {
        ...country, 
        weightedScore: parseFloat(weightedScore.toFixed(1))
      };
    });
    
    // Tri
    if (sortConfig.key) {
      result.sort((a, b) => {
        let aValue, bValue;
        
        if (sortConfig.key === 'name') {
          aValue = a[sortConfig.key];
          bValue = b[sortConfig.key];
        } else if (sortConfig.key === 'rank') {
          aValue = a[sortConfig.key];
          bValue = b[sortConfig.key]; 
        } else if (sortConfig.key === 'weightedScore') {
          aValue = a[sortConfig.key];
          bValue = b[sortConfig.key];
        } else {
          aValue = a.scores[sortConfig.key] || 0;
          bValue = b.scores[sortConfig.key] || 0;
        }
        
        if (aValue < bValue) {
          return sortConfig.direction === 'ascending' ? -1 : 1;
        }
        if (aValue > bValue) {
          return sortConfig.direction === 'ascending' ? 1 : -1;
        }
        return 0;
      });
    }
    
    setFilteredCountries(result);
  }, [countries, activeRegion, searchQuery, sortConfig, customWeights]);

  // Gestion du tri
  const requestSort = (key) => {
    let direction = 'ascending';
    if (sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    setSortConfig({ key, direction });
  };

  // Gestion de la sélection de pays pour la comparaison
  const handleCountrySelection = (countryId) => {
    setSelectedCountries(prevSelected => {
      if (prevSelected.includes(countryId)) {
        return prevSelected.filter(id => id !== countryId);
      } else {
        if (prevSelected.length < 5) { // Limite à 5 pays
          return [...prevSelected, countryId];
        }
        return prevSelected;
      }
    });
  };

  // Sauvegarde de configuration
  const saveConfiguration = () => {
    const config = {
      weights: customWeights,
      selectedRegion: activeRegion,
      selectedCountries
    };
    localStorage.setItem('rankingConfig', JSON.stringify(config));
    alert('Configuration sauvegardée avec succès !');
  };

  return (
    <div className="min-h-screen flex flex-col">
      
      <main className="flex-grow pt-20 pb-16">
        <section className="bg-purple-700 py-12 mb-8">
          <div className="container mx-auto px-4">
            <h1 className="text-3xl md:text-4xl font-bold text-white mb-4">Classement des pays africains</h1>
            <p className="text-lg text-purple-100">
              Explorez et comparez les performances des pays africains selon différents critères d'ouverture des données et de développement.
            </p>
          </div>
        </section>
        
        <div className="container mx-auto px-4">
          <div className="bg-white rounded-lg shadow-lg p-6 mb-8">
            <div className="flex flex-col lg:flex-row justify-between gap-6 mb-6">
              <div className="w-full lg:w-2/3">
                <div className="flex flex-col md:flex-row items-center gap-4 mb-6">
                  <div className="w-full md:w-1/2">
                    <div className="relative">
                      <input
                        type="text"
                        placeholder="Rechercher un pays..."
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                      />
                      <svg className="w-5 h-5 text-gray-500 absolute right-3 top-2.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                      </svg>
                    </div>
                  </div>
                  
                  <RegionalFilters 
                    activeRegion={activeRegion} 
                    setActiveRegion={setActiveRegion} 
                  />
                </div>
              </div>
              
              <ExportOptions countries={filteredCountries} weights={customWeights}/>
            </div>
            <CountryTable 
              countries={filteredCountries}
              selectedCountries={selectedCountries}
              onCountrySelect={handleCountrySelection}
              requestSort={requestSort}
              sortConfig={sortConfig}
              weights={customWeights} // Passer les poids au tableau
            />
          </div>
          
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
            <div className="lg:col-span-2">
              <RankingCharts 
                countries={countries}
                selectedCountries={selectedCountries}
                weights={customWeights} // Passer les poids aux graphiques
              />
            </div>
            
            <div className="lg:col-span-1">
              <AdvancedFilters 
                weights={customWeights}
                setWeights={setCustomWeights}
                onSaveConfig={saveConfiguration}
              />
            </div>
          </div>
        </div>
      </main>
      
    </div>
  );
};

export default RankingPage;