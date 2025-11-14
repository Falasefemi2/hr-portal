import {
  ForbiddenException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { UsersService } from 'src/users/users.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';
import { User } from 'src/users/entirires/user.entity';

@Injectable()
export class AuthService {
  constructor(
    private usersService: UsersService,
    private jwtService: JwtService,
  ) {}

  async login(loginDto: LoginDto) {
    const user = await this.usersService.findByEmployeeId(loginDto.employeeId);
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
    return this.generateToken(user);
  }

  async register(registerDto: RegisterDto, hrUser: any) {
    if (hrUser.role !== 'hr') {
      throw new ForbiddenException('Only HR can register users');
    }
    if (registerDto.role === 'hod' && !registerDto.departmentId) {
      throw new ForbiddenException('HOD must be assigned to a department');
    }
    if (registerDto.role === 'employee' && !registerDto.departmentId) {
      throw new ForbiddenException('Employee must be assigned to a department');
    }
    const newUser = await this.usersService.create(registerDto);
    return this.generateToken(newUser);
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

  private generateToken(user: Omit<User, 'password'>) {
    const payload = {
      sub: user.id,
      employeeId: user.employeeId,
      email: user.email,
      role: user.role,
      departmentId: user.departmentId,
      isActive: user.isActive,
    };

    return {
      access_token: this.jwtService.sign(payload),
      user: {
        id: user.id,
        employeeId: user.employeeId,
        email: user.email,
        role: user.role,
        departmentId: user.departmentId,
      },
    };
  }
}
