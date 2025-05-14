import React, { useState, useEffect } from 'react';

function FilterPanel({ onWeightsChange }) {
  const [weights, setWeights] = useState({});
  
  // Initialiser avec 3 critères par défaut
  useEffect(() => {
    // Si aucun poids n'est défini, initialiser avec les valeurs par défaut
    if (Object.keys(weights).length === 0) {
      const defaultWeights = {
        'odin': 40,
        'hdi': 30,
        'internet': 30
      };
      setWeights(defaultWeights);
      
      // Informer les composants parents du changement
      if (onWeightsChange) {
        onWeightsChange(defaultWeights);
      }
    }
  }, []);

  const resetFilters = () => {
    const defaultWeights = {
      'odin': 40,
      'hdi': 30,
      'internet': 30
    };
    setWeights(defaultWeights);
    
    // Informer les composants parents du changement
    if (onWeightsChange) {
      onWeightsChange(defaultWeights);
    }
  };

  const handleAddCriteria = (e) => {
    const selected = e.target.value;
    if (!selected) return;
    
    // Vérifier si le critère est déjà dans les poids
    if (weights.hasOwnProperty(selected)) {
      alert(`Le critère "${getLabel(selected)}" est déjà sélectionné.`);
      return;
    }
    
    // Calculer les nouveaux poids
    const currentTotal = Object.values(weights).reduce((sum, val) => sum + val, 0);
    const otherCriteria = Object.keys(weights);
    
    // Valeur par défaut pour le nouveau critère: 50%
    const newValue = 50;
    
    if (currentTotal + newValue > 100) {
      // Ajuster les poids existants pour faire de la place au nouveau critère
      const remaining = 100 - newValue;
      const scalingFactor = remaining / currentTotal;
      
      const newWeights = { ...weights };
      
      otherCriteria.forEach(key => {
        newWeights[key] = Math.round(weights[key] * scalingFactor);
      });
      
      // Ajouter le nouveau critère
      newWeights[selected] = newValue;
      
      // Ajuster si la somme n'est pas exactement 100 à cause des arrondis
      const totalAfterAdjustment = Object.values(newWeights).reduce((sum, val) => sum + val, 0);
      if (totalAfterAdjustment !== 100) {
        const diff = 100 - totalAfterAdjustment;
        if (otherCriteria.length > 0) {
          newWeights[otherCriteria[0]] += diff;
        } else {
          newWeights[selected] += diff;
        }
      }
      
      setWeights(newWeights);
      
      // Informer les composants parents du changement
      if (onWeightsChange) {
        onWeightsChange(newWeights);
      }
    } else {
      // Simplement ajouter le nouveau critère
      const newWeights = {
        ...weights,
        [selected]: newValue
      };
      
      setWeights(newWeights);
      
      // Informer les composants parents du changement
      if (onWeightsChange) {
        onWeightsChange(newWeights);
      }
    }
    
    // Réinitialiser le select
    e.target.value = '';
  };

  const handleWeightChange = (criteria, newValue) => {
    const otherWeightsTotal = Object.entries(weights)
      .filter(([k]) => k !== criteria)
      .reduce((sum, [, val]) => sum + val, 0);
    
    if (newValue + otherWeightsTotal > 100) {
      return; // Ne pas permettre un total > 100%
    }
    
    // Mettre à jour le poids
    const newWeights = { ...weights, [criteria]: newValue };
    
    // Ajuster les autres poids si nécessaire pour que le total soit 100%
    const remaining = 100 - newValue;
    const otherCriteria = Object.keys(weights).filter(k => k !== criteria);
    
    if (otherWeightsTotal > 0) {
      const scalingFactor = remaining / otherWeightsTotal;
      
      otherCriteria.forEach(k => {
        newWeights[k] = Math.round(weights[k] * scalingFactor);
      });
      
      // Ajuster si la somme n'est pas exactement 100 à cause des arrondis
      const totalAfterAdjustment = Object.values(newWeights).reduce((sum, val) => sum + val, 0);
      if (totalAfterAdjustment !== 100 && otherCriteria.length > 0) {
        const diff = 100 - totalAfterAdjustment;
        newWeights[otherCriteria[0]] += diff;
      }
    }
    
    setWeights(newWeights);
    
    // Informer les composants parents du changement
    if (onWeightsChange) {
      onWeightsChange(newWeights);
    }
  };

  const handleRemoveCriteria = (criteriaKey) => {
    if (Object.keys(weights).length <= 1) {
      alert("Vous devez conserver au moins un critère.");
      return;
    }
    
    const newWeights = { ...weights };
    delete newWeights[criteriaKey];
    
    // Redistribuer les poids pour que le total reste 100%
    const currentTotal = Object.values(newWeights).reduce((sum, val) => sum + val, 0);
    if (currentTotal > 0) {
      const scalingFactor = 100 / currentTotal;
      
      Object.keys(newWeights).forEach(key => {
        newWeights[key] = Math.round(newWeights[key] * scalingFactor);
      });
      
      // Ajuster si la somme n'est pas exactement 100 à cause des arrondis
      const totalAfterAdjustment = Object.values(newWeights).reduce((sum, val) => sum + val, 0);
      if (totalAfterAdjustment !== 100 && Object.keys(newWeights).length > 0) {
        const diff = 100 - totalAfterAdjustment;
        newWeights[Object.keys(newWeights)[0]] += diff;
      }
    }
    
    setWeights(newWeights);
    
    // Informer les composants parents du changement
    if (onWeightsChange) {
      onWeightsChange(newWeights);
    }
  };

  // Liste des critères disponibles avec leurs couleurs
  const availableCriteria = [
    { key: 'odin', label: 'Indice ODIN (données ouvertes)', color: 'purple' },
    { key: 'hdi', label: 'Indice de Développement Humain', color: 'green' },
    { key: 'internet', label: 'Accès à Internet', color: 'blue' },
    { key: 'education', label: 'Éducation', color: 'yellow' },
    { key: 'gdp', label: 'PIB par habitant', color: 'red' },
    { key: 'innovation', label: 'Innovation', color: 'indigo' },
    { key: 'governance', label: 'Gouvernance', color: 'pink' },
    { key: 'health', label: 'Santé', color: 'teal' },
    { key: 'environment', label: 'Environnement', color: 'emerald' }
  ];

  const getLabel = (key) => {
    const criteria = availableCriteria.find(c => c.key === key);
    return criteria ? criteria.label : key;
  };

  const getCriteriaColor = (key) => {
    const criteria = availableCriteria.find(c => c.key === key);
    return criteria ? criteria.color : 'gray';
  };
  
  const getColorClasses = (color) => {
    switch (color) {
      case 'purple':
        return 'bg-purple-100 text-purple-800';
      case 'green':
        return 'bg-green-100 text-green-800';
      case 'blue':
        return 'bg-blue-100 text-blue-800';
      case 'yellow':
        return 'bg-yellow-100 text-yellow-800';
      case 'red':
        return 'bg-red-100 text-red-800';
      case 'indigo':
        return 'bg-indigo-100 text-indigo-800';
      case 'pink':
        return 'bg-pink-100 text-pink-800';
      case 'teal':
        return 'bg-teal-100 text-teal-800';
      case 'emerald':
        return 'bg-emerald-100 text-emerald-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="bg-white p-6 rounded-xl shadow-md mb-6">
      <h3 className="text-xl font-semibold mb-4">Critères d'évaluation</h3>
      
      <div className="mb-6">
        <label htmlFor="add-criteria" className="block text-sm font-medium text-gray-700 mb-1">
          Ajouter des critères d'évaluation
        </label>
        <select
          id="add-criteria"
          defaultValue=""
          onChange={handleAddCriteria}
          className="w-full border-gray-300 rounded-md shadow-sm focus:border-purple-500 focus:ring focus:ring-purple-500 focus:ring-opacity-50"
        >
          <option value="">-- Ajouter un critère --</option>
          {availableCriteria.filter(c => !weights.hasOwnProperty(c.key)).map(criteria => (
            <option key={criteria.key} value={criteria.key}>
              {criteria.label}
            </option>
          ))}
        </select>
      </div>
      
      <h4 className="font-medium text-gray-700 mb-4">Pondération des critères</h4>
      
      {Object.keys(weights).length === 0 ? (
        <p className="text-gray-500 text-sm mb-6">Aucun critère sélectionné.</p>
      ) : (
        <div className="space-y-4 mb-6">
          {Object.entries(weights).map(([key, value]) => (
            <div key={key} className="bg-gray-50 p-3 rounded-lg">
              <div className="flex justify-between items-center mb-2">
                <div className="flex items-center">
                  <span className={`inline-block px-2 py-1 rounded text-xs ${getColorClasses(getCriteriaColor(key))}`}>
                    {key.toUpperCase()}
                  </span>
                  <span className="ml-2 text-sm font-medium text-gray-700">{getLabel(key)}</span>
                </div>
                <div className="flex items-center">
                  <span className="text-sm font-medium mr-2">{value}%</span>
                  <button 
                    onClick={() => handleRemoveCriteria(key)}
                    className="p-1 text-gray-400 hover:text-red-500 transition-colors"
                    aria-label="Supprimer ce critère"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                  </button>
                </div>
              </div>
              <input 
                type="range" 
                min="0" 
                max="100" 
                value={value}
                onChange={(e) => handleWeightChange(key, parseInt(e.target.value))}
                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
              />
            </div>
          ))}
        </div>
      )}
      
      <div className="mb-6 p-3 bg-gray-50 rounded-lg">
        <div className="flex justify-between text-sm font-medium">
          <span>Total:</span>
          <span className={Object.values(weights).reduce((sum, val) => sum + val, 0) === 100 ? "text-green-600" : "text-red-600"}>
            {Object.values(weights).reduce((sum, val) => sum + val, 0)}%
          </span>
        </div>
      </div>
      
      <button
        onClick={resetFilters}
        className="w-full py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 font-medium rounded-md transition-colors"
      >
        Réinitialiser
      </button>
    </div>
  );
}

export default FilterPanel;