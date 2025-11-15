import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class LoginDto {
  @ApiProperty({
    description: 'Employee ID',
    example: 'EMP001',
    type: String,
  })
  @IsString()
  @IsNotEmpty()
  employeeId: string;

  @ApiProperty({
    description: 'User password',
    example: 'password123',
    type: String,
    minLength: 8,
  })
  @IsString()
  @IsNotEmpty()
  password: string;
}
