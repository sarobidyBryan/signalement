/**
 * Utilitaire de compression d'images côté client.
 * Utilise le Canvas API pour redimensionner et compresser les images
 * afin d'optimiser le stockage sur le plan gratuit de Supabase.
 */

/** Taille maximale cible en octets (200 Ko) */
const MAX_FILE_SIZE = 200 * 1024;

/** Dimension maximale (largeur ou hauteur) en pixels */
const MAX_DIMENSION = 1280;

/**
 * Charge une image depuis une URL (webviewPath ou blob URL) et retourne un HTMLImageElement.
 */
function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = (err) => reject(new Error(`Impossible de charger l'image: ${err}`));
    img.src = src;
  });
}

/**
 * Calcule les nouvelles dimensions en conservant le ratio d'aspect.
 */
function calculateDimensions(
  width: number,
  height: number,
  maxDim: number
): { width: number; height: number } {
  if (width <= maxDim && height <= maxDim) {
    return { width, height };
  }

  const ratio = width / height;
  if (width > height) {
    return { width: maxDim, height: Math.round(maxDim / ratio) };
  }
  return { width: Math.round(maxDim * ratio), height: maxDim };
}

/**
 * Convertit un canvas en Blob avec la qualité JPEG spécifiée.
 */
function canvasToBlob(canvas: HTMLCanvasElement, quality: number): Promise<Blob> {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) resolve(blob);
        else reject(new Error('Échec de la conversion canvas vers blob'));
      },
      'image/jpeg',
      quality
    );
  });
}

/**
 * Compresse une image depuis son URL (webviewPath) vers un Blob JPEG
 * de taille ≤ MAX_FILE_SIZE (200 Ko par défaut).
 *
 * Stratégie :
 * 1. Redimensionner si les dimensions dépassent MAX_DIMENSION
 * 2. Réduire progressivement la qualité JPEG jusqu'à atteindre la taille cible
 *
 * @param imageUrl - URL de l'image source (webviewPath, blob:, data:, etc.)
 * @param maxSize  - Taille maximale en octets (défaut: 200 Ko)
 * @returns Blob JPEG compressé
 */
export async function compressImage(
  imageUrl: string,
  maxSize: number = MAX_FILE_SIZE
): Promise<Blob> {
  const img = await loadImage(imageUrl);

  const { width, height } = calculateDimensions(
    img.naturalWidth,
    img.naturalHeight,
    MAX_DIMENSION
  );

  const canvas = document.createElement('canvas');
  canvas.width = width;
  canvas.height = height;

  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Impossible de créer le contexte canvas 2D');

  // Dessiner l'image redimensionnée
  ctx.drawImage(img, 0, 0, width, height);

  // Compression progressive : démarrer à qualité 0.85, réduire par pas de 0.05
  let quality = 0.85;
  let blob = await canvasToBlob(canvas, quality);

  while (blob.size > maxSize && quality > 0.1) {
    quality -= 0.05;
    blob = await canvasToBlob(canvas, quality);
  }

  // Si toujours trop gros, réduire davantage les dimensions
  if (blob.size > maxSize) {
    const scaleFactor = Math.sqrt(maxSize / blob.size);
    canvas.width = Math.round(width * scaleFactor);
    canvas.height = Math.round(height * scaleFactor);
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
    blob = await canvasToBlob(canvas, 0.7);
  }

  console.log(
    `Image compressée : ${(blob.size / 1024).toFixed(1)} Ko ` +
    `(${canvas.width}x${canvas.height}, qualité ~${(quality * 100).toFixed(0)}%)`
  );

  return blob;
}
