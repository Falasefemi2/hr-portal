import {
  IsEmail,
  IsEnum,
  IsOptional,
  IsString,
  MinLength,
} from 'class-validator';
import { UserRole } from '../common/enums/role.enum';

export class CreateUserDto {
  @IsEmail()
  email: string;

  @IsString()
  @MinLength(8)
  password: string;

  @IsString()
  employeeId: string;

  @IsEnum(UserRole)
  role: UserRole;

  @IsOptional()
  @IsString()
  departmentId?: number;
}
