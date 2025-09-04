import { BadRequestException } from '@nestjs/common';
import { diskStorage } from 'multer';
import * as path from 'path';
import * as fs from 'fs';
import { Request } from 'express';

export const makeProfilePicStorageConfig = (getUserId: (req: Request) => string | number) => ({
  storage: diskStorage({
    destination: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, destination: string) => void,
    ) => {
      const userId = getUserId(req);
      const dir = `uploads/profile-pics/${userId}`;
      fs.mkdirSync(dir, { recursive: true });
      cb(null, dir);
    },
    filename: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, filename: string) => void,
    ) => {
      cb(null, file.originalname);
    },
  }),
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});

/**
 * Multer storage configuration for creating new properties with images.
 * Files are temporarily stored under `uploads/temp-property-images/{timestamp}`.
 * They will be moved to the final location after property creation.
 */
export const makePropertyCreationStorageConfig = () => ({
  storage: diskStorage({
    destination: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, destination: string) => void,
    ) => {
      const timestamp = Date.now();
      const dir = path.join('uploads', 'temp-property-images', String(timestamp));
      fs.mkdirSync(dir, { recursive: true });
      // Store the temp directory in request for later use
      (req as any).tempImageDir = dir;
      cb(null, dir);
    },
    filename: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, filename: string) => void,
    ) => {
      const ext = path.extname(file.originalname).toLowerCase() || '.jpg';
      const base = path.basename(file.originalname, ext).replace(/[^a-z0-9_-]/gi, '_');
      const timestamp = Date.now();
      cb(null, `${base}_${timestamp}${ext}`);
    },
  }),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB per file
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});

export const makePropertyImagesStorageConfig = (
  extractPropertyId: (req: Request) => string | number,
) => ({
  storage: diskStorage({
    destination: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, destination: string) => void,
    ) => {
      const propertyId = extractPropertyId(req);
      const dir = path.join('uploads', 'property-images', String(propertyId));
      fs.mkdirSync(dir, { recursive: true });
      cb(null, dir);
    },
    filename: (
      req: Request,
      file: Express.Multer.File,
      cb: (error: Error | null, filename: string) => void,
    ) => {
      const ext = path.extname(file.originalname).toLowerCase() || '.jpg';
      const base = path.basename(file.originalname, ext).replace(/[^a-z0-9_-]/gi, '_');
      const timestamp = Date.now();
      cb(null, `${base}_${timestamp}${ext}`);
    },
  }),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10 MB per file
  fileFilter: (
    req: Request,
    file: Express.Multer.File,
    cb: (error: Error | null, acceptFile: boolean) => void,
  ) => {
    if (!file.mimetype.startsWith('image/')) {
      return cb(new BadRequestException('Only image files are allowed!'), false);
    }
    cb(null, true);
  },
});