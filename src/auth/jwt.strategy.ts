import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.getOrThrow<string>('JWT_SECRET'),
    });
  }

  async validate(payload: any) {
    // Ensure user is active
    if (!payload.isActive) {
      throw new UnauthorizedException('User account is deactivated');
    }

    return {
      userId: payload.sub,
      employeeId: payload.employeeId,
      email: payload.email,
      role: payload.role,
      departmentId: payload.departmentId,
      isActive: payload.isActive,
    };
  }
}
