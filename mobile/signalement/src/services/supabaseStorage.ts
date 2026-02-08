/**
 * Service d'upload d'images vers Supabase Storage.
 * Gère la compression, l'upload et la génération des URLs publiques.
 */

import { supabase, STORAGE_BUCKET } from '@/config/supabase';
import { compressImage } from '@/utils/imageCompression';
import type { UserPhoto } from '@/composables/usePhotoGallery';

export interface ImageReport {
  id: string;
  id_report: string;
  lien: string;
}

/**
 * Génère un identifiant unique simple (sans dépendance externe).
 */
function generateId(): string {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

/**
 * Upload une seule image vers Supabase Storage.
 *
 * @param photo      - Photo à uploader (avec webviewPath)
 * @param reportId   - ID du signalement Firestore
 * @param index      - Index de la photo (pour nommage unique)
 * @returns ImageReport avec l'URL publique
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

  // 2. Construire le chemin dans le bucket : reports/{reportId}/{timestamp}_{index}.jpg
  const imageId = generateId();
  const filePath = `reports/${reportId}/${imageId}_${index}.jpg`;

  // 3. Upload vers Supabase Storage
  const { error: uploadError } = await supabase.storage
    .from(STORAGE_BUCKET)
    .upload(filePath, compressedBlob, {
      contentType: 'image/jpeg',
      cacheControl: '3600',
      upsert: false,
    });

  if (uploadError) {
    throw new Error(`Erreur upload image ${index}: ${uploadError.message}`);
  }

  // 4. Récupérer l'URL publique
  const { data: urlData } = supabase.storage
    .from(STORAGE_BUCKET)
    .getPublicUrl(filePath);

  return {
    id: imageId,
    id_report: reportId,
    lien: urlData.publicUrl,
  };
}

/**
 * Upload toutes les photos d'un signalement vers Supabase Storage.
 * Compresse chaque image avant l'upload.
 *
 * @param photos    - Tableau de UserPhoto à uploader
 * @param reportId  - ID du signalement Firestore
 * @returns Tableau d'ImageReport avec les URLs publiques
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
      console.log(`Image ${i + 1}/${photos.length} uploadée avec succès`);
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
    `Upload terminé : ${results.length}/${photos.length} image(s) uploadée(s)`
  );

  return results;
}

/**
 * Supprime toutes les images d'un signalement dans Supabase Storage.
 *
 * @param reportId - ID du signalement
 */
export async function deleteReportImages(reportId: string): Promise<void> {
  const { data: files, error: listError } = await supabase.storage
    .from(STORAGE_BUCKET)
    .list(`reports/${reportId}`);

  if (listError) {
    console.error('Erreur listage images:', listError);
    return;
  }

  if (files && files.length > 0) {
    const paths = files.map((f) => `reports/${reportId}/${f.name}`);
    const { error: removeError } = await supabase.storage
      .from(STORAGE_BUCKET)
      .remove(paths);

    if (removeError) {
      console.error('Erreur suppression images:', removeError);
    }
  }
}
