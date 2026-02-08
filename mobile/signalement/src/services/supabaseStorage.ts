/**
 * [DÉSACTIVÉ] Service d'upload d'images vers Supabase Storage.
 * Remplacé par cloudinaryStorage.ts
 * Gère la compression, l'upload et la génération des URLs publiques.
 */

// =============================================================================
// CODE SUPABASE STORAGE COMMENTÉ — Remplacé par Cloudinary
// Voir : @/services/cloudinaryStorage.ts
// =============================================================================

/*
import { supabase, STORAGE_BUCKET } from '@/config/supabase';
import { compressImage } from '@/utils/imageCompression';
import type { UserPhoto } from '@/composables/usePhotoGallery';

export interface ImageReport {
  id: string;
  id_report: string;
  lien: string;
}

function generateId(): string {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
}

async function uploadSingleImage(
  photo: UserPhoto,
  reportId: string,
  index: number
): Promise<ImageReport> {
  if (!photo.webviewPath) {
    throw new Error(`La photo ${photo.filepath} n'a pas de webviewPath`);
  }

  const compressedBlob = await compressImage(photo.webviewPath);

  const imageId = generateId();
  const filePath = `reports/${reportId}/${imageId}_${index}.jpg`;

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

  const { data: urlData } = supabase.storage
    .from(STORAGE_BUCKET)
    .getPublicUrl(filePath);

  return {
    id: imageId,
    id_report: reportId,
    lien: urlData.publicUrl,
  };
}

export async function uploadReportImages(
  photos: UserPhoto[],
  reportId: string
): Promise<ImageReport[]> {
  if (!photos || photos.length === 0) return [];

  const results: ImageReport[] = [];
  const errors: string[] = [];

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
*/
