import React, { useState } from 'react';

const CriteriaSelector = ({ weights, setWeights }) => {
  const [selectedCriteria, setSelectedCriteria] = useState('');
  
  const handleCriteriaChange = (e) => {
    const selected = e.target.value;
    if (!selected) return;
    
    setSelectedCriteria('');
    
    // Vérifier si le critère est déjà dans les poids
    if (weights.hasOwnProperty(selected)) {
      alert(`Le critère "${getCriteriaLabel(selected)}" est déjà sélectionné.`);
      return;
    }
    
    // Calculer les nouveaux poids
    const currentTotal = Object.values(weights).reduce((sum, val) => sum + val, 0);
    const otherCriteria = Object.keys(weights);
    
    // Valeur par défaut pour le nouveau critère: 30%
    const newValue = 30;
    
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
    } else {
      // Simplement ajouter le nouveau critère
      setWeights({
        ...weights,
        [selected]: newValue
      });
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
  };

  // Liste des critères disponibles
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
  
  const getCriteriaLabel = (key) => {
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
    <div className="mb-6">
      <h4 className="font-medium text-gray-700 mb-4">Ajouter des critères d'évaluation</h4>
      
      <div className="flex gap-2 mb-6">
        <select 
          value={selectedCriteria}
          onChange={handleCriteriaChange}
          className="flex-grow border-gray-300 rounded-md shadow-sm focus:border-purple-500 focus:ring focus:ring-purple-500 focus:ring-opacity-50"
        >
          <option value="">-- Ajouter un critère --</option>
          {availableCriteria.filter(c => !weights.hasOwnProperty(c.key)).map(criteria => (
            <option key={criteria.key} value={criteria.key}>
              {criteria.label}
            </option>
          ))}
        </select>
      </div>
      
      <div className="space-y-4">
        {Object.entries(weights).map(([key, value]) => (
          <div key={key} className="bg-gray-50 p-3 rounded-lg">
            <div className="flex justify-between items-center mb-2">
              <div className="flex items-center">
                <span className={`inline-block px-2 py-1 rounded text-xs ${getColorClasses(getCriteriaColor(key))}`}>
                  {key.toUpperCase()}
                </span>
                <span className="ml-2 text-sm font-medium text-gray-700">{getCriteriaLabel(key)}</span>
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
              onChange={(e) => {
                const newValue = parseInt(e.target.value);
                const otherWeightsTotal = Object.entries(weights)
                  .filter(([k]) => k !== key)
                  .reduce((sum, [, val]) => sum + val, 0);
                
                if (newValue + otherWeightsTotal > 100) {
                  return; // Ne pas permettre un total > 100%
                }
                
                // Mettre à jour le poids
                const newWeights = { ...weights, [key]: newValue };
                
                // Ajuster les autres poids si nécessaire pour que le total soit 100%
                const remaining = 100 - newValue;
                const otherCriteria = Object.keys(weights).filter(k => k !== key);
                
                if (otherWeightsTotal > 0) {
                  const scalingFactor = remaining / otherWeightsTotal;
                  
                  otherCriteria.forEach(k => {
                    newWeights[k] = Math.round(weights[k] * scalingFactor);
                  });
                  
                  // Ajuster si la somme n'est pas exactement 100 à cause des arrondis
                  const totalAfterAdjustment = Object.values(newWeights).reduce((sum, val) => sum + val, 0);
                  if (totalAfterAdjustment !== 100) {
                    const diff = 100 - totalAfterAdjustment;
                    newWeights[otherCriteria[0]] += diff;
                  }
                }
                
                setWeights(newWeights);
              }}
              className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
            />
          </div>
        ))}
      </div>
      
      <div className="mt-4 p-3 bg-gray-50 rounded-lg">
        <div className="flex justify-between text-sm font-medium">
          <span>Total:</span>
          <span className="text-green-600">
            {Object.values(weights).reduce((sum, val) => sum + val, 0)}%
          </span>
        </div>
      </div>
    </div>
  );
};

export default CriteriaSelector;