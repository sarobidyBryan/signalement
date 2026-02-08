import { createClient } from '@supabase/supabase-js';

const supabaseUrl = 'https://scmngipwizvrfvglznmg.supabase.co';
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNjbW5naXB3aXp2cmZ2Z2x6bm1nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAyODY4ODAsImV4cCI6MjA4NTg2Mjg4MH0.5u-fibP_V5WtOEdqYh7RzUPJleIZhaWu1_0vm0pnpdo';

// Nom du bucket Supabase Storage pour les images de signalements
export const STORAGE_BUCKET = 'report-images';

export const supabase = createClient(supabaseUrl, supabaseAnonKey);
