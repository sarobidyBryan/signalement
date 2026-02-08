import { ref } from 'vue';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';

export interface UserPhoto {
  filepath: string;
  webviewPath?: string;
}

export function usePhotoGallery() {
  const photos = ref<UserPhoto[]>([]);

  const takePhoto = async () => {
    try {
      const capturedPhoto = await Camera.getPhoto({
        resultType: CameraResultType.Uri,
        source: CameraSource.Camera,
        quality: 100,
      });

      const newPhoto: UserPhoto = {
        filepath: capturedPhoto.path || `photo_${Date.now()}.jpg`,
        webviewPath: capturedPhoto.webPath,
      };

      photos.value = [newPhoto, ...photos.value];
      return newPhoto;
    } catch (error) {
      console.error('Erreur lors de la prise de photo:', error);
      throw error;
    }
  };

  // FONCTION avec sélection multiple 
  const pickPhoto = async () => {
    try {
      const capturedPhotos = await Camera.pickImages({
        quality: 100,
        limit: 0, // 0 = pas de limite, ou mettez un nombre (ex: 10)
      });

      // Traiter toutes les photos sélectionnées
      const newPhotos: UserPhoto[] = capturedPhotos.photos.map((photo, index) => ({
        filepath: photo.path || `photo_${Date.now()}_${index}.jpg`,
        webviewPath: photo.webPath,
      }));

      // Ajouter toutes les nouvelles photos au début du tableau
      photos.value = [...newPhotos, ...photos.value];
      
      return newPhotos;
    } catch (error) {
      console.error('Erreur lors de l\'import de photos:', error);
      throw error;
    }
  };

  const deletePhoto = (photo: UserPhoto) => {
    photos.value = photos.value.filter(p => p.filepath !== photo.filepath);
  };

  const resetPhotos = () => {
    photos.value = [];
  };

  return {
    photos,
    takePhoto,
    pickPhoto,
    deletePhoto,
    resetPhotos,
  };
}