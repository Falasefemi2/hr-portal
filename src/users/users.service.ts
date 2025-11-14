import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entirires/user.entity';
import * as bcrypt from 'bcrypt';
import { Repository } from 'typeorm';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
  ) {}

  async create(createUserDto: CreateUserDto): Promise<Omit<User, 'password'>> {
    const existingUser = await this.findByEmployeeId(createUserDto.employeeId);
    if (existingUser) {
      throw new ConflictException('Employee ID already exists');
    }
    const hashedPassword = await bcrypt.hash(createUserDto.password, 12);
    const user = this.usersRepository.create({
      ...createUserDto,
      password: hashedPassword,
    });
    const savedUser = await this.usersRepository.save(user);
    const { password, ...result } = savedUser;
    return result;
  }

  async update(
    id: string,
    updateUserDto: UpdateUserDto,
  ): Promise<Omit<User, 'password'>> {
    if (updateUserDto.password) {
      updateUserDto.password = await bcrypt.hash(updateUserDto.password, 12);
    }
    const result = await this.usersRepository.update(id, updateUserDto);
    if (result.affected === 0) {
      throw new NotFoundException(`User with ID ${id} not found`);
    }
    const updatedUser = await this.findOne(id);
    if (!updatedUser) {
      throw new NotFoundException(`User with ID ${id} not found`);
    }
    return updatedUser;
  }

  async findAll(): Promise<Omit<User, 'password'>[]> {
    const users = await this.usersRepository.find({
      select: [
        'id',
        'employeeId',
        'email',
        'role',
        'departmentId',
        'isActive',
        'createdAt',
      ],
    });
    return users;
  }

  async findOne(id: string): Promise<User | null> {
    return this.usersRepository.findOne({
      where: { id },
      select: ['id', 'employeeId', 'email', 'role', 'departmentId', 'isActive'],
    });
  }

  async remove(id: string): Promise<void> {
    const user = await this.findOne(id);
    if (!user) {
      throw new NotFoundException(`User with ID ${id} not found`);
    }
    await this.usersRepository.update(id, { isActive: false });
  }

  async findByEmployeeId(employeeId: string): Promise<User | null> {
    return this.usersRepository.findOne({
      where: { employeeId },
      select: [
        'id',
        'email',
        'employeeId',
        'departmentId',
        'role',
        'isActive',
        'createdAt',
        'updatedAt',
      ],
    });
  }

  async findByEmployeeIdWithPassword(employeeId: string): Promise<User | null> {
    return this.usersRepository.findOne({
      where: { employeeId },
      select: [
        'id',
        'email',
        'password',
        'employeeId',
        'departmentId',
        'role',
        'isActive',
      ],
    });
  }

  async findByDepartment(
    departmentId: number,
  ): Promise<Omit<User, 'password'>[]> {
    return this.usersRepository.find({
      where: { departmentId },
      select: ['id', 'employeeId', 'email', 'role', 'departmentId'],
    });
  }

  async validatePassword(
    plainPassword: string,
    hashedPassword: string,
  ): Promise<boolean> {
    return bcrypt.compare(plainPassword, hashedPassword);
  }
}
