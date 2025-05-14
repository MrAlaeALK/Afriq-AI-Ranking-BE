import React from 'react';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

const ExportOptions = ({ countries, weights }) => {
  // Fonction utilitaire pour obtenir les en-têtes et données formatées
  const getFormattedData = () => {
    const staticHeaders = ['Rang', 'Pays', 'Score global'];
    const dynamicHeaders = Object.keys(weights).map(criterion => {
      const labels = {
        odin: 'ODIN',
        hdi: 'IDH',
        internet: 'Internet',
        education: 'Éducation',
        gdp: 'PIB',
        innovation: 'Innovation',
        governance: 'Gouvernance',
        health: 'Santé',
        environment: 'Environnement',
      };
      return labels[criterion] || criterion.toUpperCase();
    });
    const headers = [...staticHeaders, ...dynamicHeaders];

    const rows = countries.map(country => {
      const staticData = [
        country.rank,
        country.name,
        country.weightedScore || country.scores.global,
      ];
      const dynamicData = Object.keys(weights).map(criterion =>
        country.scores[criterion] !== undefined ? country.scores[criterion] : ''
      );
      return [...staticData, ...dynamicData];
    });

    return { headers, rows };
  };

  const exportCSV = () => {
    const { headers, rows } = getFormattedData();
    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.join(',')),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'afriq_ai_ranking.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const exportPDF = () => {
    const { headers, rows } = getFormattedData();

    // Créer un nouveau document PDF
    const doc = new jsPDF();

    // Ajouter un titre
    doc.setFontSize(16);
    doc.text('Classement AfriQ AI', 14, 15);

    // Ajouter la date d'exportation
    doc.setFontSize(10);
    doc.text(`Exporté le ${new Date().toLocaleDateString('fr-FR')}`, 14, 22);

    // Ajouter un tableau avec les données
    autoTable(doc, {
      head: [headers],
      body: rows,
      startY: 30,
      styles: {
        fontSize: 8,
        cellPadding: 2,
        overflow: 'linebreak',
      },
      headStyles: {
        fillColor: [44, 62, 80],
        textColor: 255,
      },
      alternateRowStyles: {
        fillColor: [240, 240, 240],
      },
      margin: { top: 30 },
    });

    // Ajouter une note en bas de page
    const pageCount = doc.internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.text(`Page ${i} sur ${pageCount}`, doc.internal.pageSize.width - 25, doc.internal.pageSize.height - 10);
    }

    // Sauvegarder le PDF
    doc.save('afriq_ai_ranking.pdf');
  };

  return (
    <div className="flex flex-col md:flex-row gap-3">
      <button
        onClick={exportCSV}
        className="flex items-center justify-center px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
      >
        <svg
          className="w-5 h-5 mr-2 text-gray-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        Exporter CSV
      </button>
      <button
        onClick={exportPDF}
        className="flex items-center justify-center px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
      >
        <svg
          className="w-5 h-5 mr-2 text-gray-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
          />
        </svg>
        Exporter PDF
      </button>
    </div>
  );
};

export default ExportOptions;