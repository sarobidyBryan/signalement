/**
 * Configuration Cloudinary pour l'upload d'images.
 * Utilise l'API Upload avec un preset non signé (unsigned upload preset).
 */

export const CLOUDINARY_CLOUD_NAME = 'dgekpcxh4';
export const CLOUDINARY_UPLOAD_PRESET = 'app_reports';

/**
 * URL de l'API Upload Cloudinary.
 */
export const CLOUDINARY_UPLOAD_URL = `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`;
