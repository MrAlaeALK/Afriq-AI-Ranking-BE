import React from 'react';
import CriteriaSelector from './CriteriaSelector';

const AdvancedFilters = ({ weights, setWeights, onWeightsChange }) => {

  const resetFilters = () => {
    const defaultWeights = {
      'odin': 40,
      'hdi': 30,
      'internet': 30
    };
    setWeights(defaultWeights);

    if(onWeightsChange){
      onWeightsChange(defaultWeights);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <h3 className="text-xl font-bold mb-6">Choisissez votre critères personnalisées</h3>
      
      <div className="mb-6">
        <h4 className="font-medium text-gray-700 mb-4">Pondération personnalisée</h4>
        <p className="text-sm text-gray-600 mb-4">
          Ajustez l'importance de chaque critère pour personnaliser le classement selon vos priorités.
        </p>
        
        {/* Nouveau composant de sélection de critères */}
        <CriteriaSelector 
          weights={weights}
          setWeights={setWeights}
        />
      </div>
      
      <div className="border-t border-gray-200 pt-6">
        <button
          onClick={resetFilters}
          className="w-full px-4 py-2 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
        >
          Réinitialiser
        </button>
      </div>
    </div>
  );
};

export default AdvancedFilters;