import { useState, useEffect } from 'react';

// Importation des composants
import CriteriaSelector from '../compare/CriteriaSelector';
import CountrySelector from '../compare/CountrySelector';
import RadarChartDisplay from '../compare/RadarChartDisplay';
import CountryCardList from '../compare/CountryCardList';
import { data, criteriaLabels } from '../compare/Data';

export default function ComparisonPage() {
  // État pour stocker les pays sélectionnés
  const [selectedCountries, setSelectedCountries] = useState([null, null, null, null, null]);
  // État pour stocker les critères sélectionnés
  const [selectedCriteria, setSelectedCriteria] = useState({
    //global: true,
    odin: true,
    hdi: true,
    internet: true,
    education: false,
    gdp: false,
    innovation: false,
    governance: false,
    health: false,
    environment: false
  });
  // Compteur de critères sélectionnés
  const [criteriaCount, setCriteriaCount] = useState(3);
  // Données pour le graphique
  const [chartData, setChartData] = useState([]);
  // Données pour chaque pays
  const [countryData, setCountryData] = useState([]);

  // Mise à jour des données du graphique lorsque les pays ou critères sélectionnés changent
  useEffect(() => {
    // Variables pour stocker les données formatées
    const newChartData = [];
    const newCountryData = [];
    
    // Obtenir tous les indicateurs disponibles
    const indicators = Object.keys(selectedCriteria).filter(key => selectedCriteria[key]);
    
    // Créer un objet pour chaque indicateur avec les valeurs de chaque pays
    indicators.forEach(indicator => {
      const dataPoint = {
        indicator: criteriaLabels[indicator] || indicator,
      };
      
      // Ajouter les données de chaque pays sélectionné
      selectedCountries.forEach((countryId) => {
        if (countryId) {
          const country = data.find(c => c.id === countryId);
          if (country) {
            // Ajouter le pays à la liste des pays s'il n'y est pas déjà
            if (!newCountryData.find(c => c.id === country.id)) {
              newCountryData.push(country);
            }
            // Ajouter la valeur de l'indicateur pour ce pays
            dataPoint[`${country.flag} ${country.name}`] = country.scores[indicator];
          }
        }
      });
      
      // N'ajouter le point que s'il y a au moins un pays sélectionné
      if (Object.keys(dataPoint).length > 1) {
        newChartData.push(dataPoint);
      }
    });
    
    setChartData(newChartData);
    setCountryData(newCountryData);
  }, [selectedCountries, selectedCriteria]);

  // Gérer le changement de pays
  const handleCountryChange = (index, countryId) => {
    const newSelectedCountries = [...selectedCountries];
    newSelectedCountries[index] = countryId ? parseInt(countryId) : null;
    setSelectedCountries(newSelectedCountries);
  };
  
  // Gérer le changement de critères
  const handleCriteriaChange = (criterion) => {
    // Calculer le nouveau nombre de critères sélectionnés
    const newCount = selectedCriteria[criterion] 
      ? criteriaCount - 1 
      : criteriaCount + 1;
    
    // Vérifier si la déselection laisserait moins de 3 critères
    if (selectedCriteria[criterion] && newCount < 3) {
      alert("Vous devez sélectionner au moins 3 critères pour le graphique radar.");
      return;
    }
    
    setSelectedCriteria({
      ...selectedCriteria,
      [criterion]: !selectedCriteria[criterion]
    });
    
    setCriteriaCount(newCount);
  };

  return (
    <div className="min-h-screen flex flex-col">

      <main className="flex-grow pt-20 pb-16">
        <section className="bg-purple-700 py-12 mb-8">
          <div className="container mx-auto px-4">
            <h1 className="text-3xl md:text-4xl font-bold text-white mb-4">Comparaison des Pays Africains</h1>
            <p className="text-lg text-purple-100">
              Explorez et comparez les performances des pays africains selon différents critères d'ouverture des données et de développement.
            </p>
          </div>
        </section>
            
        {/* Sélection des critères */}
        <CriteriaSelector 
          selectedCriteria={selectedCriteria}
          criteriaCount={criteriaCount}
          onCriteriaChange={handleCriteriaChange}
        />
        
        {/* Sélection des pays */}
        <CountrySelector 
          selectedCountries={selectedCountries}
          onCountryChange={handleCountryChange}
        />

        {/* Conteneur pour CardList (gauche) et RadarChart (droite) */}
        {countryData.length > 0 && (
          <div className="container mx-auto px-4 mb-8">
            <div className="flex flex-col lg:flex-row gap-8">
              {/* Cartes des pays (gauche) */}
              <div className="w-full lg:w-1/2">
                <CountryCardList 
                  countryData={countryData} 
                  selectedCriteria={selectedCriteria} 
                />
              </div>
              
              {/* Graphique de comparaison (droite) */}
              <div className="w-full lg:w-1/2">
                {chartData.length > 0 && criteriaCount >= 3 ? (
                  <RadarChartDisplay 
                    chartData={chartData} 
                    countryData={countryData} 
                  />
                ) : (
                  <div className="text-center p-8 bg-gray-100 rounded-lg">
                    {criteriaCount < 3 ? (
                      <p className="text-gray-500">Sélectionnez au moins 3 critères pour voir le graphique radar</p>
                    ) : (
                      <p className="text-gray-500">Sélectionnez au moins un pays pour voir les données</p>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}