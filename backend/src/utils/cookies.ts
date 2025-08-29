import { Response } from 'express';

const attachCookies = (res: Response, accessToken: String, refreshToken: String) => {

    res.cookie('accessToken', accessToken, {
        httpOnly: true,
        maxAge: 15 * 60 * 1000, // 15 minutes
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict',
    });

    res.cookie('refreshToken', refreshToken, {
        httpOnly: true,
        maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict',
    });

    return res;
}

export default attachCookies;