import {
  Controller,
  Post,
  Body,
  Get,
  UseGuards,
  Request,
  Param,
  ForbiddenException,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiBody,
} from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { Roles } from 'src/common/decorators/roles.decorator';
import { UserRole } from 'src/users/common/enums/role.enum';
import { RolesGuard } from 'src/common/guards/roles.guard';
import { RegisterDto } from './dto/register.dto';
import { JwtAuthGuard } from 'src/common/decorators/jwt-auth.guard';

@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  @ApiOperation({
    summary: 'User login',
    description: 'Authenticate user and get access tokens',
  })
  @ApiBody({ type: LoginDto })
  @ApiResponse({
    status: 200,
    description: 'Login successful',
    schema: {
      type: 'object',
      properties: {
        accessToken: {
          type: 'string',
          example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        },
        refreshToken: {
          type: 'string',
          example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Invalid credentials' })
  @ApiResponse({ status: 403, description: 'Account is deactivated' })
  async login(@Body() loginDto: LoginDto) {
    return this.authService.login(loginDto);
  }

  @Post('register')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.HR)
  @ApiBearerAuth('JWT-auth')
  @ApiOperation({
    summary: 'Register new user',
    description: 'Register a new user (HR only)',
  })
  @ApiBody({ type: RegisterDto })
  @ApiResponse({
    status: 201,
    description: 'User registered successfully',
    schema: {
      type: 'object',
      properties: {
        accessToken: {
          type: 'string',
          example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        },
        refreshToken: {
          type: 'string',
          example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        },
      },
    },
  })
  @ApiResponse({ status: 400, description: 'Bad request - validation error' })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Only HR can register users',
  })
  @ApiResponse({
    status: 409,
    description: 'Conflict - Employee ID already exists',
  })
  async register(@Body() registerDto: RegisterDto, @Request() req) {
    return this.authService.register(registerDto, req.user);
  }

  @Get('validate')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth('JWT-auth')
  @ApiOperation({
    summary: 'Validate token',
    description: 'Validate JWT token and return user info',
  })
  @ApiResponse({
    status: 200,
    description: 'Token is valid',
    schema: {
      type: 'object',
      properties: {
        valid: { type: 'boolean', example: true },
        user: {
          type: 'object',
          properties: {
            userId: { type: 'string' },
            employeeId: { type: 'string' },
            email: { type: 'string' },
            role: { type: 'string', enum: ['hr', 'hod', 'employee'] },
            departmentId: { type: 'number' },
            isActive: { type: 'boolean' },
          },
        },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Unauthorized - Invalid token' })
  validateToken(@Request() req) {
    return { valid: true, user: req.user };
  }

  @Get('profile')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth('JWT-auth')
  @ApiOperation({
    summary: 'Get user profile',
    description: 'Get current authenticated user profile',
  })
  @ApiResponse({
    status: 200,
    description: 'User profile retrieved successfully',
    schema: {
      type: 'object',
      properties: {
        userId: { type: 'string' },
        employeeId: { type: 'string' },
        email: { type: 'string' },
        role: { type: 'string', enum: ['hr', 'hod', 'employee'] },
        departmentId: { type: 'number' },
        isActive: { type: 'boolean' },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Unauthorized - Invalid token' })
  getProfile(@Request() req) {
    return req.user;
  }

  @Get('users/department/:departmentId')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth('JWT-auth')
  @ApiOperation({
    summary: 'Get users by department',
    description: 'Get all users in a specific department (HR and HOD only)',
  })
  @ApiResponse({
    status: 200,
    description: 'Users retrieved successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized - Invalid token' })
  @ApiResponse({
    status: 403,
    description: 'Forbidden - Insufficient permissions',
  })
  async getUsersByDepartment(
    @Request() req,
    @Param('departmentId') departmentId: number,
  ) {
    if (req.user.role !== 'hr' && req.user.role !== 'hod') {
      throw new ForbiddenException(
        'Only HR and HOD can view users by department',
      );
    }
    return this.authService.getUsersByDepartment(departmentId);
  }

  @Get('users/employee/:employeeId')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth('JWT-auth')
  @ApiOperation({
    summary: 'Get user by employee ID',
    description: 'Get user information by employee ID',
  })
  @ApiResponse({
    status: 200,
    description: 'User retrieved successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized - Invalid token' })
  async getUserByEmployeeId(@Param('employeeId') employeeId: string) {
    return this.authService.getUserByEmployeeId(employeeId);
  }
}
