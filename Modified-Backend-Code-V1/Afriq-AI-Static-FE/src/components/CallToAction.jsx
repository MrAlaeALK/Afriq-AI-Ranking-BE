// components/CallToAction.js
import React from 'react';

const CallToAction = () => {
  return (
    <section className="py-16 bg-purple-700 text-white">
      <div className="container mx-auto px-4 text-center">
        <h3 className="text-2xl md:text-3xl font-bold mb-6">Prêt à découvrir l'ouverture des données en Afrique?</h3>
        <p className="text-xl text-purple-100 mb-8 max-w-2xl mx-auto">Explorez notre carte interactive et comparez les performances des pays africains en matière de données ouvertes et de développement.</p>
        <div className="flex justify-center gap-4 flex-wrap">
          <a href="/carte" className="px-6 py-3 bg-white text-purple-700 font-medium rounded-lg hover:bg-purple-50 transition-colors shadow-md">
            Explorer la carte
          </a>
          <a href="#" className="px-6 py-3 bg-purple-600 text-white font-medium rounded-lg border border-purple-400 hover:bg-purple-800 transition-colors shadow-md">
            En savoir plus
          </a>
        </div>
      </div>
    </section>
  );
};

export default CallToAction;