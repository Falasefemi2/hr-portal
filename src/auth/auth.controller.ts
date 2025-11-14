import {
  Controller,
  Post,
  Body,
  Get,
  UseGuards,
  Request,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { Roles } from 'src/common/decorators/roles.decorator';
import { UserRole } from 'src/users/common/enums/role.enum';
import { RolesGuard } from 'src/common/guards/roles.guard';
import { RegisterDto } from './dto/register.dto';
import { JwtAuthGuard } from 'src/common/decorators/jwt-auth.guard';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @Post('login')
  async login(@Body() loginDto: LoginDto) {
    return this.authService.login(loginDto);
  }

  @Post('register')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.HR)
  async register(@Body() registerDto: RegisterDto, @Request() req) {
    return this.authService.register(registerDto, req.user);
  }

  @Get('validate')
  @UseGuards(JwtAuthGuard)
  validateToken(@Request() req) {
    return { valid: true, user: req.user };
  }

  @Get('profile')
  @UseGuards(JwtAuthGuard)
  getProfile(@Request() req) {
    return req.user;
  }
}
