import {
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { UsersService } from 'src/users/users.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';
import { User } from 'src/users/entirires/user.entity';
import { UserToken } from 'src/users/entirires/token.entity';
import { UserRole } from 'src/users/common/enums/role.enum';

@Injectable()
export class AuthService {
  constructor(
    private usersService: UsersService,
    private jwtService: JwtService,
    @InjectRepository(UserToken)
    private userTokenRepository: Repository<UserToken>,
  ) {}

  async login(loginDto: LoginDto) {
    const user = await this.usersService.findByEmployeeIdWithPassword(
      loginDto.employeeId,
    );
    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }
    const hashedPassword =
      user?.password || '$2b$12$dummyHashToPreventTimingAttack';
    const isValidPassword = await bcrypt.compare(
      loginDto.password,
      hashedPassword,
    );
    if (!user || !isValidPassword) {
      throw new UnauthorizedException('Invalid credentials');
    }
    if (!user.isActive) {
      throw new ForbiddenException('Account is deactivated');
    }
    const { password, ...userWithoutPassword } = user;
    return this.generateTokens(userWithoutPassword);
  }

  async register(registerDto: RegisterDto, hrUser: { role: UserRole }) {
    if (hrUser.role !== UserRole.HR) {
      throw new ForbiddenException('Only HR can register users');
    }
    if (registerDto.role === UserRole.HOD && !registerDto.departmentId) {
      throw new ForbiddenException('HOD must be assigned to a department');
    }
    if (registerDto.role === UserRole.EMPLOYEE && !registerDto.departmentId) {
      throw new ForbiddenException('Employee must be assigned to a department');
    }
    const newUser = await this.usersService.create(registerDto);
    return this.generateTokens(newUser);
  }

  async validateToken(token: string) {
    try {
      const decoded = this.jwtService.verify(token);
      const user = await this.usersService.findOne(decoded.sub);

      if (!user || !user.isActive) {
        throw new UnauthorizedException('Invalid or inactive user');
      }

      return {
        valid: true,
        user: {
          id: user.id,
          employeeId: user.employeeId,
          email: user.email,
          role: user.role,
          departmentId: user.departmentId,
        },
      };
    } catch (error) {
      return { valid: false, error: error.message };
    }
  }

  private async generateTokens(user: Omit<User, 'password'>) {
    const payload = {
      sub: user.id,
      employeeId: user.employeeId,
      email: user.email,
      role: user.role,
      departmentId: user.departmentId,
      isActive: user.isActive,
    };

    const accessToken = this.jwtService.sign(payload, { expiresIn: '15m' });
    const refreshToken = this.jwtService.sign(payload, { expiresIn: '7d' });

    // Save hashed refresh token
    const hashedToken = await bcrypt.hash(refreshToken, 12);

    // Delete existing tokens for this user
    await this.userTokenRepository.delete({ userId: user.id });

    // Save new token
    const tokenEntity = this.userTokenRepository.create({
      userId: user.id,
      refreshToken: hashedToken,
    });
    await this.userTokenRepository.save(tokenEntity);

    return { accessToken, refreshToken };
  }
}
