// components/Hero.js
import React from 'react';

const Hero = () => {
  return (
    <section className="hero-pattern pt-24 pb-16 md:pt-32 md:pb-24">
      <div className="container mx-auto px-4">
        <div className="flex flex-col md:flex-row items-center">
          <div className="w-full md:w-1/2 mb-8 md:mb-0">
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-800 mb-4">
              Découvrez le classement des pays africains par données ouvertes et plus
            </h2>
            <p className="text-lg text-gray-600 mb-8">
              Explorez les performances des 54 pays africains avec notre carte interactive et nos outils de comparaison
            </p>
            <div className="flex flex-wrap gap-4">
              <a href="/carte" className="px-6 py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors shadow-md">
                Explorer la carte
              </a>
              <a href="classement" className="px-6 py-3 bg-green-500 text-white font-medium rounded-lg hover:bg-green-600 transition-colors shadow-md">
                Voir le classement
              </a>
            </div>
          </div>
          <div className="w-full md:w-1/2">
            <div className="africa-map h-64 md:h-80 lg:h-96 mx-auto relative rounded-xl shadow-xl">
              <div className="absolute inset-0 flex items-center justify-center text-white text-2xl font-bold opacity-0 hover:opacity-100 transition-opacity duration-300 bg-black bg-opacity-30 rounded-xl">
                Explorez l'Afrique
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Hero;
