import { Injectable } from '@nestjs/common';
import {
  S3Client,
  PutObjectCommand,
  DeleteObjectCommand,
} from '@aws-sdk/client-s3';

@Injectable()
export class S3Service {
  private readonly s3Client: S3Client;
  private readonly bucketName: string;
  private readonly region: string;

  constructor() {
    this.region = process.env.AWS_REGION || 'eu-west-1';
    this.bucketName = process.env.AWS_S3_BUCKET_NAME || '';

    this.s3Client = new S3Client({
      region: this.region,
      credentials: {
        accessKeyId: process.env.AWS_ACCESS_KEY_ID || '',
        secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY || '',
      },
    });
  }

  /**
   * Upload a file to S3
   * @param buffer - The file buffer
   * @param key - The S3 object key (path in bucket)
   * @param mimeType - The file MIME type
   * @returns The public URL of the uploaded file
   */
  async uploadFile(
    buffer: Buffer,
    key: string,
    mimeType: string,
  ): Promise<string> {
    const command = new PutObjectCommand({
      Bucket: this.bucketName,
      Key: key,
      Body: buffer,
      ContentType: mimeType,
      ACL: 'public-read',
    });

    await this.s3Client.send(command);

    return this.getPublicUrl(key);
  }

  /**
   * Delete a file from S3
   * @param key - The S3 object key to delete
   */
  async deleteFile(key: string): Promise<void> {
    const command = new DeleteObjectCommand({
      Bucket: this.bucketName,
      Key: key,
    });

    await this.s3Client.send(command);
  }

  /**
   * Extract the S3 key from a full S3 URL
   * @param url - The full S3 URL
   * @returns The S3 object key
   */
  extractKeyFromUrl(url: string): string | null {
    if (!url) return null;

    // Handle URLs like https://bucket.s3.region.amazonaws.com/key
    const regex = new RegExp(
      `https?://${this.bucketName}\\.s3\\.${this.region}\\.amazonaws\\.com/(.+)`,
    );
    const match = url.match(regex);

    if (match) {
      return match[1];
    }

    // Handle URLs like https://bucket.s3.amazonaws.com/key
    const simpleRegex = new RegExp(
      `https?://${this.bucketName}\\.s3\\.amazonaws\\.com/(.+)`,
    );
    const simpleMatch = url.match(simpleRegex);

    if (simpleMatch) {
      return simpleMatch[1];
    }

    return null;
  }

  /**
   * Get the public URL for an S3 object
   * @param key - The S3 object key
   * @returns The public URL
   */
  private getPublicUrl(key: string): string {
    return `https://${this.bucketName}.s3.${this.region}.amazonaws.com/${key}`;
  }
}

