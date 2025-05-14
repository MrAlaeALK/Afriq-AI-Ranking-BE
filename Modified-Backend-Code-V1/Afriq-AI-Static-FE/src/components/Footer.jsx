import React from "react";
import FooterColumn from "./FooterColumn";


const Footer = () => {
    return (
      <footer className="bg-gray-800 text-gray-300 py-12">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h4 className="text-xl font-bold text-white mb-4">Afriq'<span className="text-green-400">AI</span></h4>
              <p className="mb-4">Plateforme d'intelligence artificielle africaine pour l'analyse et la visualisation des données ouvertes</p>
            </div>
            
            <FooterColumn title="Navigation">
              <ul className="space-y-2">
                <li><a href="/" className="hover:text-purple-300 transition-colors">Accueil</a></li>
                <li><a href="/carte" className="hover:text-purple-300 transition-colors">Carte</a></li>
                <li><a href="/classement" className="hover:text-purple-300 transition-colors">Classement</a></li>
                <li><a href="#" className="hover:text-purple-300 transition-colors">Comparer</a></li>
                <li><a href="#" className="hover:text-purple-300 transition-colors">À propos</a></li>
              </ul>
            </FooterColumn>
            
            <FooterColumn title="Ressources">
              <ul className="space-y-2">
                <li><a href="#" className="hover:text-purple-300 transition-colors">Méthodologie</a></li>
                <li><a href="#" className="hover:text-purple-300 transition-colors">Sources de données</a></li>
                <li><a href="#" className="hover:text-purple-300 transition-colors">API</a></li>
                <li><a href="#" className="hover:text-purple-300 transition-colors">Publications</a></li>
              </ul>
            </FooterColumn>
            
            <FooterColumn title="Contact">
              <ul className="space-y-2">
                <li>info@afriq-ai.org</li>
                <li>+123 456 789</li>
                <li className="flex space-x-4 mt-4">
                  <a href="https://www.linkedin.com/company/afriq-ai-institute/posts/?feedView=all" className="hover:text-purple-300 transition-colors">
                    <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path d="M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.79-1.75-1.764s.784-1.764 1.75-1.764 1.75.79 1.75 1.764-.783 1.764-1.75 1.764zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z" />
                    </svg>
                  </a>
                </li>
              </ul>
            </FooterColumn>
          </div>
          
          <div className="border-t border-gray-700 mt-8 pt-8 text-sm text-center">
            <p>&copy; 2025 Afriq'AI. Tous droits réservés.</p>
          </div>
        </div>
      </footer>
    );
  };
  
  export default Footer;
  