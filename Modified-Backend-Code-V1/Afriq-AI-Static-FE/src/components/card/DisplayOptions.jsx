// components/DisplayOptions.js
import React from 'react';

function DisplayOptions({ colorScale, setColorScale }) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-md">
      <h3 className="text-xl font-semibold mb-4">Options d'affichage</h3>
      
      <div className="mb-4">
        <label className="flex items-center">
          <input 
            type="checkbox" 
            className="rounded text-purple-600 focus:ring-purple-500" 
            defaultChecked 
          />
          <span className="ml-2 text-gray-700">Afficher les noms des pays</span>
        </label>
      </div>
      
      <div className="mb-4">
        <label className="flex items-center">
          <input 
            type="checkbox" 
            className="rounded text-purple-600 focus:ring-purple-500" 
            defaultChecked 
          />
          <span className="ml-2 text-gray-700">Afficher les scores</span>
        </label>
      </div>
      
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-1">Échelle de couleurs</label>
        <select 
          className="w-full border-gray-300 rounded-md shadow-sm focus:border-purple-500 focus:ring focus:ring-purple-500 focus:ring-opacity-50"
          value={colorScale}
          onChange={(e) => setColorScale(e.target.value)}
        >
          <option value="green-red">Vert à Rouge</option>
          <option value="blue-red">Bleu à Rouge</option>
          <option value="purple-yellow">Violet à Jaune</option>
        </select>
      </div>
    </div>
  );
}

export default DisplayOptions;
