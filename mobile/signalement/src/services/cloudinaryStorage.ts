/**
 * Service d'upload d'images vers Cloudinary.
 * Gère la compression, l'upload et la génération des URLs publiques.
 */

import { CLOUDINARY_UPLOAD_URL, CLOUDINARY_UPLOAD_PRESET } from '@/config/cloudinary';
import { compressImage } from '@/utils/imageCompression';
import type { UserPhoto } from '@/composables/usePhotoGallery';

export interface ImageReport {
  id: string;
  fireBaseReportId: string;
  postgresReportId: string;
  lien: string;
}

/**
 * Génère un identifiant unique simple (sans dépendance externe).
 */
function generateId(): string {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

/**
 * Upload une seule image vers Cloudinary.
 *
 * @param photo      - Photo à uploader (avec webviewPath)
 * @param reportId   - ID du signalement Firestore
 * @param index      - Index de la photo (pour nommage unique)
 * @returns ImageReport avec l'URL publique Cloudinary
 */
async function uploadSingleImage(
  photo: UserPhoto,
  reportId: string,
  index: number
): Promise<ImageReport> {
  if (!photo.webviewPath) {
    throw new Error(`La photo ${photo.filepath} n'a pas de webviewPath`);
  }

  // 1. Compresser l'image (≤ 200 Ko)
  const compressedBlob = await compressImage(photo.webviewPath);

  // 2. Génerer un identifiant unique pour l'image
  const imageId = generateId();

  // 3. Construire le FormData pour l'API Cloudinary
  const formData = new FormData();
  formData.append('file', compressedBlob, `${imageId}_${index}.jpg`);
  formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET);
  formData.append('folder', `reports/${reportId}`);
  formData.append('public_id', `${imageId}_${index}`);

  // 4. Upload vers Cloudinary via l'API REST (unsigned upload)
  const response = await fetch(CLOUDINARY_UPLOAD_URL, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const errorBody = await response.text();
    throw new Error(`Erreur upload image ${index}: ${response.status} - ${errorBody}`);
  }

  const data = await response.json();

  // 5. Récupérer l'URL publique sécurisée
  return {
    id: imageId,
    fireBaseReportId: reportId,
    postgresReportId: '',
    lien: data.secure_url,
  };
}

/**
 * Upload toutes les photos d'un signalement vers Cloudinary.
 * Compresse chaque image avant l'upload.
 *
 * @param photos    - Tableau de UserPhoto à uploader
 * @param reportId  - ID du signalement Firestore
 * @returns Tableau d'ImageReport avec {id, fireBaseReportId, postgresReportId, lien}
 */
export async function uploadReportImages(
  photos: UserPhoto[],
  reportId: string
): Promise<ImageReport[]> {
  if (!photos || photos.length === 0) return [];

  const results: ImageReport[] = [];
  const errors: string[] = [];

  // Upload séquentiel pour éviter de surcharger la connexion
  for (let i = 0; i < photos.length; i++) {
    try {
      const imageReport = await uploadSingleImage(photos[i], reportId, i);
      results.push(imageReport);
      console.log(`Image ${i + 1}/${photos.length} uploadée avec succès vers Cloudinary`);
    } catch (error: any) {
      console.error(`Erreur upload image ${i + 1}:`, error);
      errors.push(error.message || `Erreur image ${i + 1}`);
    }
  }

  if (errors.length > 0 && results.length === 0) {
    throw new Error(`Aucune image n'a pu être uploadée:\n${errors.join('\n')}`);
  }

  if (errors.length > 0) {
    console.warn(
      `${errors.length} image(s) en erreur sur ${photos.length}:`,
      errors
    );
  }

  console.log(
    `Upload Cloudinary terminé : ${results.length}/${photos.length} image(s) uploadée(s)`
  );

  return results;
}


